# Artifact Registry — Docker image storage
# Stores Docker images for all services
# Cloud Run pulls images from here at deploy time

resource "google_artifact_registry_repository" "healthcare" {
  repository_id = "healthcare"
  format        = "DOCKER"
  location      = var.region
  project       = var.project_id
  description   = "Healthcare AI Platform Docker images"
}
