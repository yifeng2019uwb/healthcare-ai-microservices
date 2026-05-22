#!/bin/bash
# =============================================================================
# deploy-all.sh — Deploy or destroy Healthcare AI services and infrastructure
#
# Usage:
#   ./scripts/deploy-all.sh deploy   [service|all]   — deploy services
#   ./scripts/deploy-all.sh destroy  [service|all]   — delete Cloud Run service(s)
#   ./scripts/deploy-all.sh destroy-infra            — delete services + terraform destroy
#
# Examples:
#   ./scripts/deploy-all.sh deploy all
#   ./scripts/deploy-all.sh deploy gateway
#   ./scripts/deploy-all.sh destroy all
#   ./scripts/deploy-all.sh destroy gateway
#   ./scripts/deploy-all.sh destroy-infra
# =============================================================================

set -e

GCP_PROJECT_ID="${GCP_PROJECT_ID:-healthcare-ai-yifeng}"
GCP_REGION="${GCP_REGION:-us-west1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
TERRAFORM_DIR="$(dirname "$0")/../terraform"
SERVICES_SCRIPT="$(dirname "$0")/../../scripts/deploy-services.sh"
DOCKER_DIR="$(dirname "$0")/../../docker"
SERVICES_DIR="$(dirname "$0")/../../services"

# Teardown order: gateway first (stops inbound traffic), then backends
ALL_SERVICES=("gateway" "provider-service" "patient-service" "auth-service")

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
warn()  { echo -e "${YELLOW}▶ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

ACTION="${1:-}"
TARGET="${2:-all}"

# =============================================================================
# Usage
# =============================================================================
if [[ -z "$ACTION" ]]; then
    echo ""
    echo "Usage: $0 <action> [target]"
    echo ""
    echo "Actions:"
    echo "  local                   — build JARs and start stack via Docker Compose"
    echo "  deploy  [service|all]   — build and deploy to Cloud Run"
    echo "  destroy [service|all]   — delete Cloud Run service(s)"
    echo "  destroy-infra           — delete all services then terraform destroy"
    echo ""
    echo "Services: ${ALL_SERVICES[*]}"
    echo ""
    exit 0
fi

# =============================================================================
# Helpers
# =============================================================================
service_exists() {
    gcloud run services describe "$1" \
        --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
        --format="value(metadata.name)" >/dev/null 2>&1
}

delete_service() {
    local name="${1}-${ENVIRONMENT}"
    if service_exists "$name"; then
        warn "Deleting $name..."
        gcloud run services delete "$name" \
            --region "$GCP_REGION" --project "$GCP_PROJECT_ID" --quiet
        ok "$name deleted"
    else
        ok "$name not found — skipping"
    fi
}

# =============================================================================
# Actions
# =============================================================================
case "$ACTION" in

  local)
    stage "Local Docker Compose — auth, provider, gateway"

    [[ -f "$DOCKER_DIR/.env" ]] || fail ".env not found — run: cp docker/env.example docker/.env"
    set -a; source "$DOCKER_DIR/.env"; set +a

    [[ -z "$SPRING_DATASOURCE_URL"      ]] && fail "SPRING_DATASOURCE_URL not set in docker/.env"
    [[ -z "$SPRING_DATASOURCE_USERNAME" ]] && fail "SPRING_DATASOURCE_USERNAME not set in docker/.env"
    [[ -z "$SPRING_DATASOURCE_PASSWORD" ]] && fail "SPRING_DATASOURCE_PASSWORD not set in docker/.env"
    [[ -z "$JWT_PRIVATE_KEY"            ]] && fail "JWT_PRIVATE_KEY not set in docker/.env"
    [[ -z "$JWT_PUBLIC_KEY"             ]] && fail "JWT_PUBLIC_KEY not set in docker/.env"
    ok "Environment loaded"

    stage "Building JARs"
    warn "shared..."
    (cd "$SERVICES_DIR/shared" && mvn install -DskipTests -q) && ok "shared"
    for svc in auth-service provider-service gateway; do
      warn "$svc..."
      (cd "$SERVICES_DIR/$svc" && mvn clean package -DskipTests -q) && ok "$svc"
    done

    stage "Starting stack"
    cd "$DOCKER_DIR"
    docker compose down 2>/dev/null || true
    docker compose up --build
    ;;

  deploy)
    stage "Deploy — $TARGET"
    [[ -f "$SERVICES_SCRIPT" ]] || fail "deploy-services.sh not found at $SERVICES_SCRIPT"
    "$SERVICES_SCRIPT" "$TARGET"
    ;;

  destroy)
    stage "Destroy Cloud Run — $TARGET"
    if [[ "$TARGET" == "all" ]]; then
        for svc in "${ALL_SERVICES[@]}"; do
            delete_service "$svc"
        done
        ok "All Cloud Run services deleted"
    else
        delete_service "$TARGET"
    fi
    ;;

  destroy-infra)
    stage "Full teardown — services + infrastructure"
    warn "This will destroy ALL Cloud Run services and ALL GCP infrastructure."
    read -rp "Type 'yes' to confirm: " confirm
    [[ "$confirm" == "yes" ]] || { echo "Aborted."; exit 0; }

    stage "Step 1 — Deleting Cloud Run services"
    for svc in "${ALL_SERVICES[@]}"; do
        delete_service "$svc"
    done

    stage "Step 2 — Waiting for DB connections to drain"
    warn "Waiting 15 seconds..."
    sleep 15
    ok "Done"

    stage "Step 3 — Terraform destroy"
    cd "$TERRAFORM_DIR" && ./run-terraform.sh destroy
    ;;

  *)
    fail "Unknown action: $ACTION — use deploy, destroy, or destroy-infra"
    ;;
esac

echo -e "\n${GREEN}=== Done ===${NC}\n"
