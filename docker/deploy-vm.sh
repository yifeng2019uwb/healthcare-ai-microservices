#!/bin/bash
# =============================================================================
# deploy-vm.sh — Build locally and deploy to Oracle Cloud VMs
#
# Usage:
#   ./docker/deploy-vm.sh
#
# Env overrides:
#   SSH_KEY=~/.ssh/id_rsa     path to SSH private key (default: ~/.ssh/id_rsa)
#   SSH_USER=opc              VM login user (default: opc)
#   VM1_IP=<ip>               skip Pulumi lookup and use this IP for instance-1
#   VM2_IP=<ip>               skip Pulumi lookup and use this IP for instance-2
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/.."
SERVICES_DIR="$ROOT_DIR/services"
PULUMI_DIR="$ROOT_DIR/healthcare-infra/pulumi-oracle"

SSH_KEY="${SSH_KEY:-$HOME/.ssh/oracle_vm}"
SSH_USER="${SSH_USER:-opc}"
SSH_OPTS="-i $SSH_KEY -o StrictHostKeyChecking=no -o ConnectTimeout=30 -o ServerAliveInterval=15 -o ServerAliveCountMax=8"

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }
warn()  { echo -e "${YELLOW}▶ $1${NC}"; }

# ── VM IPs ───────────────────────────────────────────────────────────────────
stage "Resolving VM IPs"
if [[ -z "$VM1_IP" ]]; then
  VM1_IP=$(cd "$PULUMI_DIR" && pulumi stack output instance1PublicIp 2>/dev/null) \
    || fail "Cannot get instance1PublicIp — run 'make oracle-up' first, or set VM1_IP=<ip>"
fi
if [[ -z "$VM2_IP" ]]; then
  VM2_IP=$(cd "$PULUMI_DIR" && pulumi stack output instance2PublicIp 2>/dev/null) \
    || fail "Cannot get instance2PublicIp — run 'make oracle-up' first, or set VM2_IP=<ip>"
fi
# Private IP for gateway→backend routing — stays on internal VCN network
VM2_PRIVATE_IP="${VM2_PRIVATE_IP:-$(cd "$PULUMI_DIR" && pulumi stack output instance2PrivateIp 2>/dev/null)}"
ok "VM1 (gateway + auth):    $VM1_IP"
ok "VM2 (provider + ai):     $VM2_IP"

# ── Prerequisites ────────────────────────────────────────────────────────────
[[ -f "$ROOT_DIR/docker/.env" ]]  || fail ".env not found — cp docker/env.example docker/.env and fill in values"
[[ -f "$SSH_KEY" ]]               || fail "SSH key not found: $SSH_KEY — set SSH_KEY=/path/to/key"

# ── Build ────────────────────────────────────────────────────────────────────
stage "Building JARs"
cd "$SERVICES_DIR"
mvn -pl auth-service,provider-service,ai-service,gateway -am clean package -DskipTests -q
ok "All JARs built"

# ── Remote directory setup ───────────────────────────────────────────────────
stage "Creating remote directories"
ssh $SSH_OPTS $SSH_USER@$VM1_IP \
  "mkdir -p ~/healthcare/docker ~/healthcare/services/gateway/target ~/healthcare/services/auth-service/target"
ssh $SSH_OPTS $SSH_USER@$VM2_IP \
  "mkdir -p ~/healthcare/docker ~/healthcare/services/provider-service/target ~/healthcare/services/ai-service/target"
ok "Directories ready"

# ── Copy Dockerfiles ─────────────────────────────────────────────────────────
stage "Copying Dockerfiles"
scp $SSH_OPTS "$SERVICES_DIR/gateway/Dockerfile"          $SSH_USER@$VM1_IP:~/healthcare/services/gateway/
scp $SSH_OPTS "$SERVICES_DIR/auth-service/Dockerfile"     $SSH_USER@$VM1_IP:~/healthcare/services/auth-service/
scp $SSH_OPTS "$SERVICES_DIR/provider-service/Dockerfile" $SSH_USER@$VM2_IP:~/healthcare/services/provider-service/
scp $SSH_OPTS "$SERVICES_DIR/ai-service/Dockerfile"       $SSH_USER@$VM2_IP:~/healthcare/services/ai-service/
ok "Dockerfiles copied"

# ── Free buffer cache before uploads ─────────────────────────────────────────
stage "Freeing buffer cache on VMs"
ssh $SSH_OPTS $SSH_USER@$VM1_IP "sudo sh -c 'sync; echo 3 > /proc/sys/vm/drop_caches'"
ssh $SSH_OPTS $SSH_USER@$VM2_IP "sudo sh -c 'sync; echo 3 > /proc/sys/vm/drop_caches'"
ok "Buffer cache cleared"

# ── Upload JARs ──────────────────────────────────────────────────────────────
stage "Uploading JARs to VM1"
rsync -az --partial -e "ssh $SSH_OPTS" "$SERVICES_DIR/gateway/target/"*.jar      $SSH_USER@$VM1_IP:~/healthcare/services/gateway/target/
rsync -az --partial -e "ssh $SSH_OPTS" "$SERVICES_DIR/auth-service/target/"*.jar $SSH_USER@$VM1_IP:~/healthcare/services/auth-service/target/
ok "VM1 JARs uploaded"

stage "Uploading JARs to VM2"
rsync -az --partial -e "ssh $SSH_OPTS" "$SERVICES_DIR/provider-service/target/"*.jar $SSH_USER@$VM2_IP:~/healthcare/services/provider-service/target/
rsync -az --partial -e "ssh $SSH_OPTS" "$SERVICES_DIR/ai-service/target/"*.jar       $SSH_USER@$VM2_IP:~/healthcare/services/ai-service/target/
ok "VM2 JARs uploaded"

# ── Copy .env ────────────────────────────────────────────────────────────────
stage "Copying .env"
# VM1 needs BACKEND_VM_IP so the gateway can route to provider + ai on VM2 via internal VCN
{ cat "$ROOT_DIR/docker/.env"; echo "BACKEND_VM_IP=$VM2_PRIVATE_IP"; } \
  | ssh $SSH_OPTS $SSH_USER@$VM1_IP "cat > ~/healthcare/docker/.env"
scp $SSH_OPTS "$ROOT_DIR/docker/.env" $SSH_USER@$VM2_IP:~/healthcare/docker/.env
ok ".env files written"

# ── Copy compose files ───────────────────────────────────────────────────────
stage "Copying compose files"
scp $SSH_OPTS "$SCRIPT_DIR/compose-gateway.yml" $SSH_USER@$VM1_IP:~/healthcare/docker/
scp $SSH_OPTS "$SCRIPT_DIR/compose-backend.yml" $SSH_USER@$VM2_IP:~/healthcare/docker/
ok "Compose files copied"

# ── Start services ───────────────────────────────────────────────────────────
# Start VM2 first — gateway routes to VM2, so backend must be up first
stage "Starting VM2 (provider + ai)"
ssh $SSH_OPTS $SSH_USER@$VM2_IP \
  "cd ~/healthcare/docker && sudo docker compose -f compose-backend.yml build provider-service && sudo docker compose -f compose-backend.yml build ai-service && sudo docker compose -f compose-backend.yml up -d"
ok "VM2 services started"

stage "Starting VM1 (gateway + auth)"
ssh $SSH_OPTS $SSH_USER@$VM1_IP \
  "cd ~/healthcare/docker && sudo docker compose -f compose-gateway.yml build auth-service && sudo docker compose -f compose-gateway.yml build gateway && sudo docker compose -f compose-gateway.yml up -d"
ok "VM1 services started"

# ── Health check ─────────────────────────────────────────────────────────────
stage "Health check"
warn "Waiting 30s for services to initialise..."
sleep 30
curl -sf "http://$VM1_IP:8080/actuator/health" | grep -q '"status":"UP"' \
  && ok "Gateway healthy at http://$VM1_IP:8080" \
  || { echo ""; fail "Gateway not healthy — check logs with: ssh $SSH_USER@$VM1_IP 'cd ~/healthcare/docker && docker compose -f compose-gateway.yml logs'"; }

echo ""
echo -e "${GREEN}=== Deploy complete ===${NC}"
echo "  Gateway:      http://$VM1_IP:8080"
echo "  SSH VM1:      ssh $SSH_USER@$VM1_IP"
echo "  SSH VM2:      ssh $SSH_USER@$VM2_IP"
echo ""
