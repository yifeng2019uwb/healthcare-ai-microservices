#!/bin/bash
# =============================================================================
# run-terraform.sh — Terraform wrapper for Healthcare AI Platform
# Usage:
#   ./run-terraform.sh validate
#   ./run-terraform.sh plan
#   ./run-terraform.sh apply
#   ./run-terraform.sh destroy
#
# Secret management (set-secrets) is intentionally excluded.
# See docs/GCP_INFRASTRUCTURE_DESIGN.md — Secret Management section.
# =============================================================================

set -e

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage(){ echo -e "\n${BLUE}=== $1 ===${NC}"; }

COMMAND=${1:-plan}

# =============================================================================
# Load .env if running locally (not in CI)
# =============================================================================
if [[ -z "$CI" ]] && [[ -f ".env" ]]; then
  source .env
  ok "Loaded .env"
fi

# =============================================================================
# Validate required env vars
# =============================================================================
stage "Pre-flight checks"

required_vars=(
  "TF_VAR_project_id"
  "TF_VAR_environment"
  "TF_VAR_region"
  "TF_VAR_db_password"
  "TF_BACKEND_BUCKET"
  "TF_BACKEND_PREFIX"
)

for var in "${required_vars[@]}"; do
  [[ -z "${!var}" ]] && fail "$var is not set — add to .env or GitHub Secrets"
done
ok "All required env vars present"

# =============================================================================
# Check secrets have values in Secret Manager
# Skip in CI — secrets managed via GitHub Secrets
# =============================================================================
if [[ -z "$CI" ]]; then
  required_secrets=("db-password" "jwt-secret" "firebase-service-account" "firebase-project-id")
  for secret in "${required_secrets[@]}"; do
    count=$(gcloud secrets versions list "$secret" \
      --project="$TF_VAR_project_id" \
      --filter="state=ENABLED" \
      --format="value(name)" 2>/dev/null | wc -l | tr -d ' ')
    if [[ "$count" -eq 0 ]]; then
      warn "Secret '$secret' has no value — add via: echo -n 'value' | gcloud secrets versions add $secret --data-file=- --project=$TF_VAR_project_id"
    else
      ok "Secret '$secret' has value"
    fi
  done
fi

# =============================================================================
# Init
# =============================================================================
stage "Terraform init"
terraform init \
  -backend-config="bucket=${TF_BACKEND_BUCKET}" \
  -backend-config="prefix=${TF_BACKEND_PREFIX}" \
  -reconfigure \
  -input=false
ok "Initialized"

# =============================================================================
# Commands
# =============================================================================
case $COMMAND in

  validate)
    stage "Validate"
    terraform validate
    ok "Config valid"
    ;;

  plan)
    stage "Plan"
    terraform plan -input=false
    ;;

  apply)
    stage "Apply"
    if [[ -z "$CI" ]]; then
      read -rp "Apply changes to ${TF_VAR_environment}? (yes/no): " confirm
      [[ "$confirm" == "yes" ]] || { echo "Aborted."; exit 0; }
    fi
    terraform apply -input=false -auto-approve
    ok "Apply complete"

    # Print outputs — copy relevant values to .env and GitHub Secrets
    stage "Outputs"
    terraform output
    ;;

  destroy)
    stage "Destroy"
    warn "This will destroy all ${TF_VAR_environment} infrastructure."
    read -rp "Type 'yes' to confirm: " confirm
    [[ "$confirm" == "yes" ]] || { echo "Aborted."; exit 0; }
    terraform destroy -input=false -auto-approve
    ok "Destroyed"
    ;;

  *)
    fail "Unknown command: $COMMAND — use validate, plan, apply, destroy"
    ;;
esac

echo -e "\n${GREEN}=== Done ===${NC}\n"
