#!/bin/bash
# =============================================================================
# setup-vm.sh — Install Docker on Oracle Cloud VMs after provisioning
#
# Run once after make oracle-up:
#   ./docker/setup-vm.sh
#
# Env overrides:
#   SSH_KEY=~/.ssh/oracle_vm     path to SSH private key
#   VM1_IP=<ip>                  skip Pulumi lookup
#   VM2_IP=<ip>                  skip Pulumi lookup
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$SCRIPT_DIR/.."
PULUMI_DIR="$ROOT_DIR/healthcare-infra/pulumi-oracle"

SSH_KEY="${SSH_KEY:-$HOME/.ssh/oracle_vm}"
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

RED='\033[0;31m'; GREEN='\033[0;32m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

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
}

setup_vm "$VM1_IP" "healthcare-gateway" &
PID1=$!
setup_vm "$VM2_IP" "healthcare-backend" &
PID2=$!
wait $PID1 && wait $PID2

echo ""
echo -e "${GREEN}=== VM setup complete ===${NC}"
echo "Next: ./docker/deploy-vm.sh"
echo ""
