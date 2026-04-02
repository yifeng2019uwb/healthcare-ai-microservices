# GCP APIs — all services required by this project
# Terraform manages enablement so nothing needs to be done manually

locals {
  required_apis = [
    "run.googleapis.com",               # Cloud Run
    "cloudbuild.googleapis.com",        # Cloud Build (gcloud run deploy --source)
    "artifactregistry.googleapis.com",  # Artifact Registry (Docker images)
    "secretmanager.googleapis.com",     # Secret Manager (JWT keys, DB passwords)
    "sqladmin.googleapis.com",          # Cloud SQL
    "redis.googleapis.com",             # Cloud Memorystore Redis
    "vpcaccess.googleapis.com",         # Serverless VPC Access (Cloud Run → VPC)
    "servicenetworking.googleapis.com", # VPC peering (Cloud SQL private IP)
    "storage.googleapis.com",           # Cloud Storage (Terraform state, Cloud Build)
  ]
}

resource "google_project_service" "apis" {
  for_each = toset(local.required_apis)

  project            = var.project_id
  service            = each.value
  disable_on_destroy = false
}
