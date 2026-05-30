#!/bin/bash
# =============================================================================
# deploy-all.sh — Build JARs and start local Docker Compose stack
#
# Usage:
#   ./healthcare-infra/scripts/deploy-all.sh
# =============================================================================

set -e

DOCKER_DIR="$(dirname "$0")/../../docker"
SERVICES_DIR="$(dirname "$0")/../../services"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
warn()  { echo -e "${YELLOW}▶ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

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
for svc in auth-service provider-service ai-service gateway; do
  warn "$svc..."
  (cd "$SERVICES_DIR/$svc" && mvn clean package -DskipTests -q) && ok "$svc"
done

stage "Starting stack"
cd "$DOCKER_DIR"
docker compose down 2>/dev/null || true
docker compose up --build

echo -e "\n${GREEN}=== Done ===${NC}\n"
