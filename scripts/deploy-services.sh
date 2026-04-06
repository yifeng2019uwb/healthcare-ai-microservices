#!/bin/bash
# =============================================================================
# deploy-services.sh — Build, package, and deploy services to Cloud Run
# =============================================================================
# Usage:
#   ./scripts/deploy-services.sh <service|all> [service2 ...]
#
# Examples:
#   ./scripts/deploy-services.sh patient-service
#   ./scripts/deploy-services.sh auth-service patient-service
#   ./scripts/deploy-services.sh all
#
# Gateway always deploys last — it reads URLs of other services at deploy time.
# Add a new service: add a build_<name>() and deploy_<name>() function below,
# then add it to ALL_SERVICES and DEPLOY_ORDER.
# =============================================================================

set -e

# =============================================================================
# CONFIG
# =============================================================================
GCP_PROJECT_ID="${GCP_PROJECT_ID:-healthcare-ai-yifeng}"
GCP_REGION="${GCP_REGION:-us-west1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
ARTIFACT_REGISTRY="$GCP_REGION-docker.pkg.dev/$GCP_PROJECT_ID/healthcare"
TERRAFORM_DIR="./healthcare-infra/terraform"
SERVICES_DIR="./services"

# Deployment order — gateway must be last
ALL_SERVICES=("auth-service" "patient-service" "provider-service" "appointment-service" "gateway")
DEPLOY_ORDER=("auth-service" "patient-service" "provider-service" "appointment-service" "gateway")

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
warn()  { echo -e "${YELLOW}▶ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

# =============================================================================
# ARGUMENT PARSING
# =============================================================================
if [[ $# -eq 0 ]]; then
  echo ""
  echo "Usage: $0 <service|all> [service2 ...]"
  echo ""
  echo "Services: ${ALL_SERVICES[*]}"
  echo ""
  echo "Examples:"
  echo "  $0 patient-service                       # redeploy one service"
  echo "  $0 auth-service patient-service          # redeploy two services"
  echo "  $0 all                                   # redeploy everything"
  echo ""
  exit 0
fi

# Resolve requested services — expand "all", validate names
REQUESTED=()
for arg in "$@"; do
  if [[ "$arg" == "all" ]]; then
    REQUESTED=("${ALL_SERVICES[@]}")
    break
  fi
  valid=false
  for s in "${ALL_SERVICES[@]}"; do
    [[ "$arg" == "$s" ]] && valid=true && break
  done
  $valid || fail "Unknown service: $arg  (known: ${ALL_SERVICES[*]})"
  REQUESTED+=("$arg")
done

# Preserve deployment order even if user specified services in a different order
TO_DEPLOY=()
for svc in "${DEPLOY_ORDER[@]}"; do
  for req in "${REQUESTED[@]}"; do
    [[ "$svc" == "$req" ]] && TO_DEPLOY+=("$svc") && break
  done
done

# =============================================================================
# PRE-FLIGHT
# =============================================================================
stage "Pre-flight"

[[ -z "$JAVA_HOME" ]] && export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
command -v java  >/dev/null 2>&1 || fail "Java not found"
command -v mvn   >/dev/null 2>&1 || fail "Maven not found"
command -v gcloud >/dev/null 2>&1 || fail "gcloud not found"
gcloud auth print-access-token >/dev/null 2>&1 || fail "Not authenticated — run: gcloud auth login"
command -v terraform >/dev/null 2>&1 || fail "Terraform not found (needed for infra values)"

ok "Java, Maven, gcloud, terraform"
ok "Deploying: ${TO_DEPLOY[*]}"

gcloud auth configure-docker "$GCP_REGION-docker.pkg.dev" --quiet

# =============================================================================
# TERRAFORM OUTPUTS
# =============================================================================
tf_output() {
  (cd "$TERRAFORM_DIR" && terraform output -raw "$1" 2>/dev/null)
}

stage "Reading infra values from Terraform"
REDIS_HOST=$(tf_output redis_host)
DB_PRIVATE_IP=$(tf_output cloud_sql_private_ip)
VPC_CONNECTOR=$(tf_output vpc_connector_id)
CLOUD_RUN_SA=$(tf_output cloud_run_sa_email)
[[ -z "$DB_PRIVATE_IP"  ]] && fail "cloud_sql_private_ip empty — run terraform apply first"
[[ -z "$VPC_CONNECTOR"  ]] && fail "vpc_connector_id empty — run terraform apply first"
[[ -z "$CLOUD_RUN_SA"   ]] && fail "cloud_run_sa_email empty — run terraform apply first"
ok "DB=$DB_PRIVATE_IP  Redis=$REDIS_HOST  VPC=$VPC_CONNECTOR"

# =============================================================================
# BUILD SHARED (once, before any service)
# =============================================================================
stage "Building shared module"
(cd "$SERVICES_DIR" && mvn install -N -q)
(cd "$SERVICES_DIR/shared" && mvn install -DskipTests -q)
ok "shared installed"

GIT_SHA=$(git rev-parse --short HEAD)

# =============================================================================
# PER-SERVICE BUILD + DEPLOY FUNCTIONS
# Add build_<name>() and deploy_<name>() when adding a new service.
# =============================================================================

build_auth_service() {
  (cd "$SERVICES_DIR/auth-service" && mvn clean package -DskipTests -q)
}

build_patient_service() {
  (cd "$SERVICES_DIR/patient-service" && mvn clean package -DskipTests -q)
}

build_provider_service() {
  (cd "$SERVICES_DIR/provider-service" && mvn clean package -DskipTests -q)
}

build_appointment_service() {
  (cd "$SERVICES_DIR/appointment-service" && mvn clean package -DskipTests -q)
}

build_gateway() {
  (cd "$SERVICES_DIR/gateway" && mvn clean package -DskipTests -q)
}

# ------------------------------------------------------------------

deploy_auth_service() {
  local image="$ARTIFACT_REGISTRY/auth-service:$GIT_SHA"
  gcloud builds submit --tag "$image" "$SERVICES_DIR/auth-service/" --quiet
  gcloud run deploy "auth-service-${ENVIRONMENT}" \
    --image "$image" \
    --region "$GCP_REGION" \
    --project "$GCP_PROJECT_ID" \
    --service-account "$CLOUD_RUN_SA" \
    --set-env-vars "ENVIRONMENT=${ENVIRONMENT}" \
    --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_PRIVATE_IP}/healthcare" \
    --set-env-vars "SPRING_DATASOURCE_USERNAME=postgres" \
    --set-env-vars "REDIS_HOST=${REDIS_HOST},REDIS_PORT=6379" \
    --set-env-vars "JWT_KEY_ID=auth-key-v1" \
    --set-secrets "SPRING_DATASOURCE_PASSWORD=db-password:latest" \
    --set-secrets "JWT_PRIVATE_KEY=jwt-private-key:latest" \
    --set-secrets "JWT_PUBLIC_KEY=jwt-public-key:latest" \
    --vpc-connector "$VPC_CONNECTOR" \
    --ingress internal \
    --allow-unauthenticated \
    --quiet
}

deploy_patient_service() {
  local image="$ARTIFACT_REGISTRY/patient-service:$GIT_SHA"
  gcloud builds submit --tag "$image" "$SERVICES_DIR/patient-service/" --quiet
  gcloud run deploy "patient-service-${ENVIRONMENT}" \
    --image "$image" \
    --region "$GCP_REGION" \
    --project "$GCP_PROJECT_ID" \
    --service-account "$CLOUD_RUN_SA" \
    --set-env-vars "ENVIRONMENT=${ENVIRONMENT}" \
    --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_PRIVATE_IP}/healthcare" \
    --set-env-vars "SPRING_DATASOURCE_USERNAME=postgres" \
    --set-secrets "SPRING_DATASOURCE_PASSWORD=db-password:latest" \
    --vpc-connector "$VPC_CONNECTOR" \
    --ingress internal \
    --allow-unauthenticated \
    --quiet
}

deploy_provider_service() {
  local image="$ARTIFACT_REGISTRY/provider-service:$GIT_SHA"
  gcloud builds submit --tag "$image" "$SERVICES_DIR/provider-service/" --quiet
  gcloud run deploy "provider-service-${ENVIRONMENT}" \
    --image "$image" \
    --region "$GCP_REGION" \
    --project "$GCP_PROJECT_ID" \
    --service-account "$CLOUD_RUN_SA" \
    --set-env-vars "ENVIRONMENT=${ENVIRONMENT}" \
    --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_PRIVATE_IP}/healthcare" \
    --set-env-vars "SPRING_DATASOURCE_USERNAME=postgres" \
    --set-secrets "SPRING_DATASOURCE_PASSWORD=db-password:latest" \
    --vpc-connector "$VPC_CONNECTOR" \
    --ingress internal \
    --allow-unauthenticated \
    --quiet
}

deploy_appointment_service() {
  local image="$ARTIFACT_REGISTRY/appointment-service:$GIT_SHA"
  gcloud builds submit --tag "$image" "$SERVICES_DIR/appointment-service/" --quiet
  gcloud run deploy "appointment-service-${ENVIRONMENT}" \
    --image "$image" \
    --region "$GCP_REGION" \
    --project "$GCP_PROJECT_ID" \
    --service-account "$CLOUD_RUN_SA" \
    --set-env-vars "ENVIRONMENT=${ENVIRONMENT}" \
    --set-env-vars "SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_PRIVATE_IP}/healthcare" \
    --set-env-vars "SPRING_DATASOURCE_USERNAME=postgres" \
    --set-secrets "SPRING_DATASOURCE_PASSWORD=db-password:latest" \
    --vpc-connector "$VPC_CONNECTOR" \
    --ingress internal \
    --allow-unauthenticated \
    --quiet
}

deploy_gateway() {
  local image="$ARTIFACT_REGISTRY/gateway:$GIT_SHA"

  AUTH_SERVICE_URL=$(gcloud run services describe "auth-service-${ENVIRONMENT}" \
    --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
    --format="value(status.url)" 2>/dev/null)
  [[ -z "$AUTH_SERVICE_URL" ]] && fail "auth-service-${ENVIRONMENT} not deployed yet — deploy auth-service first"

  PATIENT_SERVICE_URL=$(gcloud run services describe "patient-service-${ENVIRONMENT}" \
    --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
    --format="value(status.url)" 2>/dev/null)
  [[ -z "$PATIENT_SERVICE_URL" ]] && fail "patient-service-${ENVIRONMENT} not deployed yet — deploy patient-service first"

  PROVIDER_SERVICE_URL=$(gcloud run services describe "provider-service-${ENVIRONMENT}" \
    --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
    --format="value(status.url)" 2>/dev/null)
  [[ -z "$PROVIDER_SERVICE_URL" ]] && fail "provider-service-${ENVIRONMENT} not deployed yet — deploy provider-service first"

  APPOINTMENT_SERVICE_URL=$(gcloud run services describe "appointment-service-${ENVIRONMENT}" \
    --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
    --format="value(status.url)" 2>/dev/null)
  [[ -z "$APPOINTMENT_SERVICE_URL" ]] && fail "appointment-service-${ENVIRONMENT} not deployed yet — deploy appointment-service first"

  gcloud builds submit --tag "$image" "$SERVICES_DIR/gateway/" --quiet
  gcloud run deploy "gateway-${ENVIRONMENT}" \
    --image "$image" \
    --region "$GCP_REGION" \
    --project "$GCP_PROJECT_ID" \
    --service-account "$CLOUD_RUN_SA" \
    --set-env-vars "ENVIRONMENT=${ENVIRONMENT}" \
    --set-env-vars "AUTH_SERVICE_URL=${AUTH_SERVICE_URL}" \
    --set-env-vars "PATIENT_SERVICE_URL=${PATIENT_SERVICE_URL}" \
    --set-env-vars "PROVIDER_SERVICE_URL=${PROVIDER_SERVICE_URL}" \
    --set-env-vars "APPOINTMENT_SERVICE_URL=${APPOINTMENT_SERVICE_URL}" \
    --set-env-vars "REDIS_HOST=${REDIS_HOST},REDIS_PORT=6379" \
    --set-secrets "JWT_PUBLIC_KEY=jwt-public-key:latest" \
    --vpc-connector "$VPC_CONNECTOR" \
    --vpc-egress all-traffic \
    --allow-unauthenticated \
    --quiet
}

# =============================================================================
# MAIN LOOP
# =============================================================================
for svc in "${TO_DEPLOY[@]}"; do
  stage "$svc"
  fn="${svc//-/_}"   # auth-service → auth_service

  warn "Building JAR..."
  "build_${fn}"
  ok "JAR built"

  warn "Deploying to Cloud Run..."
  "deploy_${fn}"
  ok "$svc deployed → ${svc}-${ENVIRONMENT}"
done

echo -e "\n${GREEN}=== Done ===${NC}\n"
