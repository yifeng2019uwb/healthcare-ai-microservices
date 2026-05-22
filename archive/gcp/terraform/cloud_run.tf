# Cloud Run IAM — service invocation permissions
#
# Cloud Run services are deployed and configured by scripts/deploy-services.sh.
# Terraform manages only the IAM bindings (infrastructure concern, not app concern).

# Gateway — public (any client can call)
# Set by --allow-unauthenticated in deploy-services.sh; no Terraform resource needed.

# Internal services — only callable by cloud_run_sa (the gateway's runtime identity)

resource "google_cloud_run_v2_service_iam_member" "auth_service_invoker" {
  project  = var.project_id
  location = var.region
  name     = "auth-service-${var.environment}"
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_cloud_run_v2_service_iam_member" "patient_service_invoker" {
  project  = var.project_id
  location = var.region
  name     = "patient-service-${var.environment}"
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

resource "google_cloud_run_v2_service_iam_member" "appointment_service_invoker" {
  project  = var.project_id
  location = var.region
  name     = "appointment-service-${var.environment}"
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}
