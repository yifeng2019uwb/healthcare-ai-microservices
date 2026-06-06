#!/bin/bash
# =============================================================================
# setup.sh — Configure OCI credentials and Pulumi stack from .env
#
# Usage:
#   cp env.example .env        # fill in your values
#   ./setup.sh
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

GREEN='\033[0;32m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

[[ -f "$SCRIPT_DIR/.env" ]] || fail ".env not found — run: cp env.example .env and fill in your values"

set -a; source "$SCRIPT_DIR/.env"; set +a

# ── Validate required fields ─────────────────────────────────────────────────
for var in OCI_USER_OCID OCI_FINGERPRINT OCI_TENANCY_OCID OCI_REGION OCI_KEY_FILE \
           COMPARTMENT_ID AVAILABILITY_DOMAIN SSH_PUBLIC_KEY_FILE VM_CONSOLE_PASSWORD; do
  [[ -z "${!var}" ]] && fail "$var is not set in .env"
done

SSH_KEY_PATH="${SSH_PUBLIC_KEY_FILE/#\~/$HOME}"
[[ -f "$SSH_KEY_PATH" ]] || fail "SSH_PUBLIC_KEY_FILE not found: $SSH_PUBLIC_KEY_FILE"
SSH_PUBLIC_KEY=$(cat "$SSH_KEY_PATH")

[[ -f "${OCI_KEY_FILE/#\~/$HOME}" ]] || fail "OCI_KEY_FILE not found: $OCI_KEY_FILE"

# ── Write ~/.oci/config ───────────────────────────────────────────────────────
stage "Writing ~/.oci/config"
mkdir -p ~/.oci
cat > ~/.oci/config << EOF
[DEFAULT]
user=${OCI_USER_OCID}
fingerprint=${OCI_FINGERPRINT}
tenancy=${OCI_TENANCY_OCID}
region=${OCI_REGION}
key_file=${OCI_KEY_FILE}
EOF
chmod 600 ~/.oci/config
ok "~/.oci/config written"

# ── Set Pulumi stack config ───────────────────────────────────────────────────
stage "Setting Pulumi stack config"
cd "$SCRIPT_DIR"
pulumi config set compartmentId      "$COMPARTMENT_ID"
pulumi config set availabilityDomain "$AVAILABILITY_DOMAIN"
pulumi config set --secret sshPublicKey "$SSH_PUBLIC_KEY"
pulumi config set --secret vmPassword   "$VM_CONSOLE_PASSWORD"
ok "Pulumi config set"

echo -e "\n${GREEN}=== Setup complete ===${NC}"
echo "Next: make oracle-up   (from healthcare-infra/)"
echo ""
