#!/bin/bash
# =============================================================================
# LEGACY — Oracle Free Tier account terminated 2026-06-06. All VMs deleted.
# Kept for reference if Oracle Cloud VMs are used again.
# =============================================================================
# setup-vm.sh — Install Docker + eBPF EDR agent on Oracle Cloud VMs
#
# Run once after make oracle-up:
#   ./docker/setup-vm.sh
#
# Env overrides:
#   SSH_KEY=~/.ssh/oracle_vm          path to SSH private key
#   VM1_IP=<ip>                       skip Pulumi lookup
#   VM2_IP=<ip>                       skip Pulumi lookup
#   EBPF_SA_KEY_FILE=/tmp/oracle-agent.json   path to GCP SA key for eBPF agent
#                                     Get via: cd ebpf-edr-demo/infra &&
#                                       pulumi stack output oracleAgentKey --show-secrets | base64 -d > /tmp/oracle-agent.json
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/.."
PULUMI_DIR="$ROOT_DIR/healthcare-infra/pulumi-oracle"

SSH_KEY="${SSH_KEY:-$HOME/.ssh/oracle_vm}"
EBPF_SA_KEY_FILE="${EBPF_SA_KEY_FILE:-}"
EBPF_RELEASE_URL="https://raw.githubusercontent.com/yifengzh/ebpf-edr-demo/main/ebpf-edr"
SSH_USER="opc"
SSH_OPTS="-i $SSH_KEY -o StrictHostKeyChecking=no -o ConnectTimeout=60 -o ServerAliveInterval=10 -o ServerAliveCountMax=6"

ssh_retry() {
  local max=10 delay=15 attempt=0
  until ssh $SSH_OPTS "$@"; do
    attempt=$((attempt + 1))
    [[ $attempt -ge $max ]] && fail "SSH failed after $max attempts: $*"
    echo "  Retrying in ${delay}s (attempt $attempt/$max)..."
    sleep $delay
  done
}

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }
warn()  { echo -e "${YELLOW}▶ $1${NC}"; }

# ── VM IPs ───────────────────────────────────────────────────────────────────
stage "Resolving VM IPs"
if [[ -z "$VM1_IP" ]]; then
  VM1_IP=$(cd "$PULUMI_DIR" && pulumi stack output instance1PublicIp 2>/dev/null) \
    || fail "Cannot get instance1PublicIp — run 'make oracle-up' first"
fi
if [[ -z "$VM2_IP" ]]; then
  VM2_IP=$(cd "$PULUMI_DIR" && pulumi stack output instance2PublicIp 2>/dev/null) \
    || fail "Cannot get instance2PublicIp — run 'make oracle-up' first"
fi
ok "VM1: $VM1_IP"
ok "VM2: $VM2_IP"

[[ -f "$SSH_KEY" ]] || fail "SSH key not found: $SSH_KEY"

setup_vm() {
  local IP=$1
  local HOSTNAME=$2
  local ENV_TAG=$3

  stage "Setting up $HOSTNAME ($IP)"

  ssh_retry $SSH_USER@$IP "sudo hostnamectl set-hostname $HOSTNAME"
  ok "Hostname set"

  ssh_retry $SSH_USER@$IP "sudo systemctl disable --now firewalld"
  ok "Firewalld disabled"

  # Add swap before dnf — 1GB RAM is not enough for dnf metadata resolution
  ssh_retry $SSH_USER@$IP "
    if ! sudo swapon --show | grep -q /swapfile; then
      sudo fallocate -l 4G /swapfile
      sudo chmod 600 /swapfile
      sudo mkswap /swapfile
      sudo swapon /swapfile
      echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
    fi
  "
  ok "Swap ready"

  # Podman is in OL9 base repos — disable the 229MB OCI-specific repo to avoid OOM
  ssh_retry $SSH_USER@$IP "sudo dnf install -y --disablerepo='ol9_oci_included' podman podman-docker"
  ok "Podman installed"

  # Docker Compose v2 as a single binary — no dnf involved
  ssh_retry $SSH_USER@$IP "
    sudo mkdir -p /usr/local/lib/docker/cli-plugins
    sudo curl -fsSL https://github.com/docker/compose/releases/download/v2.27.0/docker-compose-linux-x86_64 \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
  "
  ok "Docker Compose v2 installed"

  # Enable podman socket with opc group access (persists across reboots)
  ssh_retry $SSH_USER@$IP "
    sudo mkdir -p /etc/systemd/system/podman.socket.d
    printf '[Socket]\nSocketGroup=opc\nSocketMode=0660\n' | sudo tee /etc/systemd/system/podman.socket.d/opc-access.conf
    sudo systemctl daemon-reload
    sudo systemctl enable --now podman.socket
    echo 'DOCKER_HOST=unix:///run/podman/podman.sock' | sudo tee -a /etc/environment
  "
  ok "Podman socket ready"

  # ── eBPF EDR agent ───────────────────────────────────────────────────────
  if [[ -n "$EBPF_SA_KEY_FILE" ]]; then
    [[ -f "$EBPF_SA_KEY_FILE" ]] || fail "EBPF_SA_KEY_FILE not found: $EBPF_SA_KEY_FILE"

    # Copy SA key
    scp -i "$SSH_KEY" -o StrictHostKeyChecking=no "$EBPF_SA_KEY_FILE" $SSH_USER@$IP:/tmp/ebpf-creds.json
    ssh_retry $SSH_USER@$IP "
      sudo mv /tmp/ebpf-creds.json /etc/ebpf-creds.json
      sudo chown root:root /etc/ebpf-creds.json
      sudo chmod 600 /etc/ebpf-creds.json
    "

    # Download binary from GitHub releases
    ssh_retry $SSH_USER@$IP "
      sudo curl -fsSL $EBPF_RELEASE_URL -o /usr/local/bin/ebpf-edr
      sudo chmod +x /usr/local/bin/ebpf-edr
      sudo restorecon -v /usr/local/bin/ebpf-edr 2>/dev/null || true
    "

    # Install systemd service
    ssh_retry $SSH_USER@$IP "
      sudo tee /etc/systemd/system/ebpf-edr.service > /dev/null << 'EOF'
[Unit]
Description=eBPF EDR Agent
After=podman.socket
Wants=podman.socket

[Service]
Type=simple
ExecStart=/usr/local/bin/ebpf-edr --runtime=docker
Restart=on-failure
RestartSec=5
Environment=GOOGLE_CLOUD_PROJECT=ebpfagent
Environment=GOOGLE_APPLICATION_CREDENTIALS=/etc/ebpf-creds.json
Environment=ENV=$ENV_TAG

[Install]
WantedBy=multi-user.target
EOF
      sudo systemctl daemon-reload
      sudo systemctl enable ebpf-edr
      sudo systemctl start ebpf-edr
    "
    ok "eBPF EDR agent installed (env=$ENV_TAG)"
  else
    warn "EBPF_SA_KEY_FILE not set — skipping eBPF agent install"
  fi
}

setup_vm "$VM1_IP" "healthcare-gateway" "oracle-vm1" &
PID1=$!
setup_vm "$VM2_IP" "healthcare-backend" "oracle-vm2" &
PID2=$!
wait $PID1 && wait $PID2

echo ""
echo -e "${GREEN}=== VM setup complete ===${NC}"
echo "Next: ./docker/deploy-vm.sh"
[[ -z "$EBPF_SA_KEY_FILE" ]] && echo "eBPF agent: re-run with EBPF_SA_KEY_FILE=/tmp/oracle-agent.json to install"
echo ""
