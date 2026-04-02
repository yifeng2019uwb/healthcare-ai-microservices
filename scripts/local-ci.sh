#!/bin/bash
# =============================================================================
# local-ci.sh — Healthcare AI Platform Local CI/CD
# =============================================================================
# Single script for all local pipeline stages.
# Replaces: test-ci.sh (build/test stages absorbed here)
# Extends with: GCP stages (terraform, schema, data, deploy, integration, zap)
#
# Usage:
#   ./scripts/local-ci.sh                         # show help
#   ./scripts/local-ci.sh --setup                 # install Java + Maven
#   ./scripts/local-ci.sh --build                 # compile all services
#   ./scripts/local-ci.sh --test                  # unit tests + coverage
#   ./scripts/local-ci.sh --terraform             # terraform plan (no changes)
#   ./scripts/local-ci.sh --terraform --apply     # terraform plan + apply
#   ./scripts/local-ci.sh --schema                # deploy DB schema to Cloud SQL
#   ./scripts/local-ci.sh --data                  # generate + load Synthea data
#   ./scripts/local-ci.sh --deploy                # build images + deploy Cloud Run
#   ./scripts/local-ci.sh --integration           # integration tests
#   ./scripts/local-ci.sh --zap                   # OWASP ZAP security scan
#   ./scripts/local-ci.sh --all                   # full pipeline
#
# Combine any stages:
#   ./scripts/local-ci.sh --build --test                   # before every push
#   ./scripts/local-ci.sh --terraform --apply --schema     # infra + schema change
#   ./scripts/local-ci.sh --schema --data                  # data model change
#   ./scripts/local-ci.sh --build --test --deploy          # deploy new code
#   ./scripts/local-ci.sh --all --skip-zap                 # full, skip slow ZAP
# =============================================================================

set -e

# =============================================================================
# CONFIG — edit for your project
# =============================================================================
GCP_PROJECT_ID="healthcare-ai-yifeng"
GCP_REGION="us-west1"
ENVIRONMENT="${ENVIRONMENT:-dev}"
CLOUD_SQL_INSTANCE="$GCP_PROJECT_ID:$GCP_REGION:healthcare-db-$ENVIRONMENT"
DB_NAME="healthcare"
DB_USER="postgres"
ARTIFACT_REGISTRY="$GCP_REGION-docker.pkg.dev/$GCP_PROJECT_ID/healthcare"
SYNTHEA_JAR="./healthcare-infra/synthea/synthea-with-dependencies.jar"
SYNTHEA_OUTPUT="./healthcare-infra/synthea/output"
TERRAFORM_DIR="./healthcare-infra/terraform"
SERVICES_DIR="./services"
GATEWAY_URL="${GATEWAY_URL:-https://gateway-dev-qgjl6kwqia-uw.a.run.app}"
# Services to build/deploy
SERVICES=("auth-service" "gateway")
PROXY_PID=""
# =============================================================================

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'

# Stage flags
RUN_SETUP=false
RUN_BUILD=false
RUN_TEST=false
RUN_TERRAFORM=false
RUN_SCHEMA=false
RUN_DATA=false
RUN_DEPLOY=false
RUN_INTEGRATION=false
RUN_ZAP=false
TERRAFORM_APPLY=false

# =============================================================================
# ARGUMENT PARSING
# =============================================================================
if [[ $# -eq 0 ]]; then
  echo ""
  echo "Healthcare AI Platform — Local CI/CD"
  echo ""
  echo "Stages:"
  echo "  --setup           Install Java 17 + Maven (run once)"
  echo "  --build           Compile all services"
  echo "  --test            Unit tests + coverage (shared module)"
  echo "  --terraform       Terraform plan — no changes made"
  echo "  --schema          Deploy DB schema to Cloud SQL"
  echo "  --data            Generate Synthea data + load into Cloud SQL"
  echo "  --deploy          Build Docker images + deploy to Cloud Run"
  echo "  --integration     Integration tests against Cloud SQL"
  echo "  --zap             OWASP ZAP security scan against Cloud Run"
  echo "  --all             Full pipeline"
  echo ""
  echo "Options:"
  echo "  --apply           With --terraform: run terraform apply"
  echo "  --skip-zap        With --all: skip ZAP scan"
  echo "  --skip-integration  With --all: skip integration tests"
  echo ""
  echo "Common day-to-day usage:"
  echo "  --build --test                     Changed Spring Boot code"
  echo "  --terraform                        Changed infra (check only)"
  echo "  --terraform --apply --schema       Changed infra + schema"
  echo "  --schema --data                    Changed data model"
  echo "  --build --test --deploy            Ready to deploy"
  echo "  --all --skip-zap                   Full pipeline before pushing"
  echo ""
  exit 0
fi

for arg in "$@"; do
  case $arg in
    --all)              RUN_BUILD=true; RUN_TEST=true; RUN_TERRAFORM=true
                        RUN_SCHEMA=true; RUN_DATA=true; RUN_DEPLOY=true
                        RUN_INTEGRATION=true; RUN_ZAP=true ;;
    --setup)            RUN_SETUP=true ;;
    --build)            RUN_BUILD=true ;;
    --test)             RUN_TEST=true ;;
    --terraform)        RUN_TERRAFORM=true ;;
    --schema)           RUN_SCHEMA=true ;;
    --data)             RUN_DATA=true ;;
    --deploy)           RUN_DEPLOY=true ;;
    --integration)      RUN_INTEGRATION=true ;;
    --zap)              RUN_ZAP=true ;;
    --apply)            TERRAFORM_APPLY=true ;;
    --skip-integration) RUN_INTEGRATION=false ;;
    --skip-zap)         RUN_ZAP=false ;;
    *)                  echo "Unknown flag: $arg — run without args to see help"; exit 1 ;;
  esac
done

# =============================================================================
# HELPERS
# =============================================================================
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
warn()  { echo -e "${YELLOW}⚠ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }

start_proxy() {
  if [[ -n "$PROXY_PID" ]]; then return; fi  # already running
  stage "Starting Cloud SQL Auth Proxy"
  [[ -f "./cloud-sql-proxy" ]] || {
    warn "Downloading cloud-sql-proxy..."
    curl -sLo cloud-sql-proxy \
      https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.0/cloud-sql-proxy.linux.amd64
    chmod +x cloud-sql-proxy
  }
  ./cloud-sql-proxy "$CLOUD_SQL_INSTANCE" --port=5432 &
  PROXY_PID=$!
  sleep 3
  pg_isready -h 127.0.0.1 -p 5432 -U "$DB_USER" >/dev/null 2>&1 || fail "Cloud SQL proxy failed to start"
  ok "Proxy running (PID $PROXY_PID)"
}

stop_proxy() {
  [[ -n "$PROXY_PID" ]] && kill "$PROXY_PID" 2>/dev/null && ok "Proxy stopped"
}
trap stop_proxy EXIT

preflight() {
  stage "Pre-flight checks"

  # Project structure — from existing test-ci.sh
  [[ -d "services" ]]                  || fail "services/ directory missing"
  [[ -d "services/shared" ]]           || fail "services/shared/ missing"
  [[ -d "services/gateway" ]]          || fail "services/gateway/ missing"
  [[ -d "services/patient-service" ]]  || fail "services/patient-service/ missing"
  [[ -f "services/dev.sh" ]]           || fail "services/dev.sh missing"
  [[ -f ".gitignore" ]]                || fail ".gitignore missing"
  ok "Project structure"

  # Java + Maven only needed for build/test/integration
  if $RUN_BUILD || $RUN_TEST || $RUN_INTEGRATION; then
    [[ -z "$JAVA_HOME" ]] && export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
    command -v java >/dev/null 2>&1 || fail "Java not found — run: ./scripts/local-ci.sh --setup"
    java -version 2>&1 | grep -qE "17|21|25" || warn "Java may not be 17+ — check: java -version"
    command -v mvn >/dev/null 2>&1 || fail "Maven not found — run: ./scripts/local-ci.sh --setup"
    ok "Java + Maven"
  fi

  # GCP auth only needed for GCP stages
  if $RUN_TERRAFORM || $RUN_SCHEMA || $RUN_DATA || $RUN_DEPLOY || $RUN_INTEGRATION || $RUN_ZAP; then
    command -v gcloud >/dev/null 2>&1 || fail "gcloud not found — install Google Cloud SDK"
    gcloud auth print-access-token >/dev/null 2>&1 || fail "Not authenticated — run: gcloud auth login"
    ok "GCP auth"
  fi

  if $RUN_TERRAFORM; then
    command -v terraform >/dev/null 2>&1 || fail "Terraform not found"
    ok "Terraform"
  fi

  if $RUN_DATA; then
    [[ -f "$SYNTHEA_JAR" ]] || fail "Synthea JAR not found at $SYNTHEA_JAR"
    command -v psql >/dev/null 2>&1 || fail "psql not found"
    ok "Synthea + psql"
  fi

  if $RUN_DEPLOY || $RUN_ZAP; then
    command -v docker >/dev/null 2>&1 || fail "Docker not found"
    ok "Docker"
  fi
}

# =============================================================================
# STAGES
# =============================================================================

run_setup() {
  stage "Setup — Install Java 17 + Maven"
  if [[ "$OSTYPE" == "darwin"* ]]; then
    command -v brew >/dev/null 2>&1 || fail "Homebrew not found"
    brew list openjdk@17 &>/dev/null || brew install openjdk@17
    command -v mvn &>/dev/null || brew install maven
    if [[ -z "$JAVA_HOME" ]]; then
      echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 17)' >> ~/.zshrc
      warn "JAVA_HOME added to ~/.zshrc — run: source ~/.zshrc"
    fi
  elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    sudo apt update -q
    sudo apt install -y openjdk-17-jdk maven
  else
    fail "Unsupported OS — install Java 17 and Maven manually"
  fi
  ok "Setup complete"
}

run_build() {
  stage "Build — compile all services"
  cd "$SERVICES_DIR"

  # Matches exact sequence from existing test-ci.sh
  echo "Installing parent POM..."
  mvn clean install -N -q

  echo "Installing shared module..."
  cd shared && mvn clean install -q && cd ..

  echo "Building services..."
  for svc in "${SERVICES[@]}"; do
    ./dev.sh "$svc" build
  done

  cd ..
  ok "All services compiled"
}

run_test() {
  stage "Unit tests"
  cd "$SERVICES_DIR"

  # Shared module with coverage — matches existing test-ci.sh
  echo "Running shared module tests with coverage..."
  ./dev.sh shared coverage

  # Service tests — warn on failure for skeleton services (matches existing behavior)
  for svc in gateway auth-service patient-service; do
    [[ -d "$svc" ]] || continue
    echo "Testing $svc..."
    ./dev.sh "$svc" test && ok "$svc tests passed" || warn "$svc tests failed (skeleton service)"
  done

  cd ..
}

run_terraform() {
  stage "Terraform"
  COMMAND="plan"
  $TERRAFORM_APPLY && COMMAND="apply"
  (cd "$TERRAFORM_DIR" && ./run-terraform.sh "$COMMAND")
}

run_schema() {
  stage "Database schema"
  (cd healthcare-infra/schema && ./run-schema.sh "$@")
}

run_data() {
  stage "Synthea data — generate + load"

  warn "Generating 200 patients (Washington / Seattle)..."
  java -jar "$SYNTHEA_JAR" \
    -p 200 \
    --exporter.csv.export=true \
    --exporter.fhir.export=false \
    --exporter.baseDirectory="$SYNTHEA_OUTPUT" \
    Washington "Seattle"
  ok "Data generated → $SYNTHEA_OUTPUT/csv/"

  start_proxy

  DB_PASS="$(gcloud secrets versions access latest \
    --secret=db-password --project="$GCP_PROJECT_ID")"

  # Load Phase 1 tables only — matches current data model
  for table in patients organizations providers encounters conditions allergies; do
    CSV="$SYNTHEA_OUTPUT/csv/$table.csv"
    if [[ -f "$CSV" ]]; then
      PGPASSWORD="$DB_PASS" psql -h 127.0.0.1 -U "$DB_USER" -d "$DB_NAME" \
        -c "\COPY $table FROM '$CSV' CSV HEADER"
      ok "Loaded $table"
    else
      warn "$table.csv not found — skipped"
    fi
  done
}

run_deploy() {
  stage "Deploy to Cloud Run"
  gcloud auth configure-docker "$GCP_REGION-docker.pkg.dev" --quiet

  GIT_SHA=$(git rev-parse --short HEAD)

  # Install parent POM + shared once before building any service
  (cd services && mvn install -N -q && cd shared && mvn install -DskipTests -q)

  for svc in "${SERVICES[@]}"; do
    [[ -d "services/$svc" ]] || { warn "services/$svc not found — skipped"; continue; }
    warn "Building $svc..."

    (cd "services/$svc" && mvn clean package -DskipTests -q)

    IMAGE="$ARTIFACT_REGISTRY/$svc:$GIT_SHA"
    gcloud builds submit --tag "$IMAGE" "services/$svc/"

    warn "Deploying $svc..."
    ./scripts/deploy-services.sh "$svc" "$IMAGE"

    ok "$svc deployed → $svc-$ENVIRONMENT"
  done
}

run_integration() {
  stage "Integration tests"
  command -v python3 >/dev/null 2>&1 || fail "python3 not found"
  python3 -c "import requests" 2>/dev/null || fail "requests not installed — run: pip install requests"

  for test in integration_tests/**/*.py; do
    [[ -f "$test" ]] || continue
    warn "Running $test..."
    GATEWAY_URL="$GATEWAY_URL" python3 "$test" && ok "$test passed" || fail "$test failed"
  done
}

run_zap() {
  stage "OWASP ZAP security scan"

  SERVICE_URL=$(gcloud run services describe gateway \
    --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
    --format='value(status.url)' 2>/dev/null)
  [[ -z "$SERVICE_URL" ]] && fail "Could not get gateway URL — is it deployed?"

  mkdir -p .zap/reports
  warn "Scanning $SERVICE_URL ..."

  docker run --rm \
    -v "$(pwd)/.zap:/zap/wrk:rw" \
    ghcr.io/zaproxy/zaproxy:stable \
    zap-baseline.py \
      -t "$SERVICE_URL" \
      -r "reports/zap-$(date +%Y%m%d-%H%M).html" \
      -I

  ok "ZAP scan complete — report in .zap/reports/"
}

# =============================================================================
# MAIN
# =============================================================================
preflight

$RUN_SETUP       && run_setup
$RUN_BUILD       && run_build
$RUN_TEST        && run_test
$RUN_TERRAFORM   && run_terraform
$RUN_SCHEMA      && run_schema
$RUN_DATA        && run_data
$RUN_DEPLOY      && run_deploy
$RUN_INTEGRATION && run_integration
$RUN_ZAP         && run_zap

echo -e "\n${GREEN}=== Done ===${NC}\n"