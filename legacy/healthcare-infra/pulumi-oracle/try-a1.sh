#!/bin/bash
# try-a1.sh — Keep trying to provision an Oracle Ampere A1 instance (4 OCPUs, 24GB) until capacity opens.
#
# SAFE: uses --target so existing VMs are NEVER touched.
#
# Usage:
#   cd healthcare-infra/pulumi-oracle
#   ./try-a1.sh
#
#   RETRY_INTERVAL=60 ./try-a1.sh   # faster retry for testing
#
# Once successful: pulumi stack output a1PublicIp

set -eo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RETRY_INTERVAL=${RETRY_INTERVAL:-60}  # 1 minutes loop
A1_URN="urn:pulumi:dev::healthcare-oracle-infra::oci:Core/instance:Instance::healthcare-a1"

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }
warn() { echo -e "${YELLOW}▶ $1${NC}"; }

cd "$SCRIPT_DIR"

if ! pulumi config get vmPassword &>/dev/null; then
  warn "vmPassword not in config — extracting from existing VM state..."
  VM_PASSWORD=$(pulumi stack export 2>/dev/null | python3 -c "
import sys, json, base64
state = json.load(sys.stdin)
for r in state.get('deployment', {}).get('resources', []):
    ud = r.get('outputs', {}).get('metadata', {}).get('user_data', '')
    if ud:
        try:
            text = base64.b64decode(ud).decode()
            for line in text.splitlines():
                if 'chpasswd' in line:
                    print(line.split(':')[1].replace(\"'\", '').strip())
                    break
        except: pass
" 2>/dev/null)
  [[ -n "$VM_PASSWORD" ]] || fail "Could not extract vmPassword — set manually."
  pulumi config set --secret vmPassword "$VM_PASSWORD"
  ok "vmPassword restored"
fi

pulumi config set a1Enabled true
ok "a1Enabled set"

attempt=0
while true; do
  attempt=$((attempt + 1))
  echo ""
  warn "Attempt $attempt — $(date '+%Y-%m-%d %H:%M:%S')"

  # --target: ONLY creates the A1 instance — existing VMs are never touched
  # --skip-preview: skip preview step for faster retry cycle
  if pulumi up --yes --target "$A1_URN" --skip-preview --suppress-outputs 2>&1 | tee /tmp/pulumi-a1.log; then
    ok "Success! A1 24GB Instance created!"
    echo ""
    pulumi stack output a1PublicIp
    exit 0
  fi

  if grep -qiE "out of host capacity|out of capacity|limit|internalerror|500" /tmp/pulumi-a1.log; then
    warn "OCI Capacity locked. Sleeping for $RETRY_INTERVAL seconds..."
    sleep $RETRY_INTERVAL
  elif grep -qiE "409|conflict|another update" /tmp/pulumi-a1.log; then
    warn "Pulumi update conflict — cancelling lock and retrying..."
    pulumi cancel --yes 2>/dev/null || true
    sleep 5
  else
    fail "Unexpected engine error encountered — check /tmp/pulumi-a1.log"
  fi
done
