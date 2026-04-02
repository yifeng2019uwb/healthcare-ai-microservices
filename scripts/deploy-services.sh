#!/bin/bash
# =============================================================================
# deploy-services.sh — Per-service Cloud Run deployment configuration
# =============================================================================
# Called by local-ci.sh run_deploy stage.
# Add a new service or change config here — local-ci.sh pipeline steps stay unchanged.
#
# Usage (called by local-ci.sh):
#   ./scripts/deploy-services.sh <service-name> <image>
#
# Example:
#   ./scripts/deploy-services.sh auth-service us-west1-docker.pkg.dev/.../auth-service:abc123
# =============================================================================

set -e

SVC="$1"
IMAGE="$2"

if [[ -z "$SVC" || -z "$IMAGE" ]]; then
  echo "Usage: $0 <service-name> <image>"
  exit 1
fi

# Inherit from caller (local-ci.sh exports these) or fall back to defaults
GCP_PROJECT_ID="${GCP_PROJECT_ID:-healthcare-ai-yifeng}"
GCP_REGION="${GCP_REGION:-us-west1}"
ENVIRONMENT="${ENVIRONMENT:-dev}"
TERRAFORM_DIR="./healthcare-infra/terraform"

# Read dynamic infra values from Terraform outputs
tf_output() {
  (cd "$TERRAFORM_DIR" && terraform output -raw "$1" 2>/dev/null)
}

REDIS_HOST=$(tf_output redis_host)
DB_PRIVATE_IP=$(tf_output cloud_sql_private_ip)
VPC_CONNECTOR=$(tf_output vpc_connector_id)
CLOUD_RUN_SA=$(tf_output cloud_run_sa_email)

# =============================================================================
# Per-service deploy functions
# Add a new function here when adding a new service.
# =============================================================================

deploy_auth_service() {
  gcloud run deploy "auth-service-${ENVIRONMENT}" \
    --image "$IMAGE" \
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

deploy_gateway() {
  AUTH_SERVICE_URL=$(gcloud run services describe "auth-service-${ENVIRONMENT}" \
    --region "$GCP_REGION" --project "$GCP_PROJECT_ID" \
    --format="value(status.url)" 2>/dev/null)

  [[ -z "$AUTH_SERVICE_URL" ]] && { echo "ERROR: auth-service-${ENVIRONMENT} not deployed yet"; exit 1; }

  gcloud run deploy "gateway-${ENVIRONMENT}" \
    --image "$IMAGE" \
    --region "$GCP_REGION" \
    --project "$GCP_PROJECT_ID" \
    --service-account "$CLOUD_RUN_SA" \
    --set-env-vars "ENVIRONMENT=${ENVIRONMENT}" \
    --set-env-vars "AUTH_SERVICE_URL=${AUTH_SERVICE_URL}" \
    --set-env-vars "REDIS_HOST=${REDIS_HOST},REDIS_PORT=6379" \
    --set-secrets "JWT_PUBLIC_KEY=jwt-public-key:latest" \
    --vpc-connector "$VPC_CONNECTOR" \
    --vpc-egress all-traffic \
    --allow-unauthenticated \
    --quiet
}

deploy_patient_service() {
  gcloud run deploy "patient-service-${ENVIRONMENT}" \
    --image "$IMAGE" \
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

# =============================================================================
# Dispatch
# =============================================================================

case "$SVC" in
  auth-service)    deploy_auth_service ;;
  gateway)         deploy_gateway ;;
  patient-service) deploy_patient_service ;;
  *) echo "Unknown service: $SVC — add deploy_${SVC//-/_}() in scripts/deploy-services.sh"; exit 1 ;;
esac
