# Outputs — useful values after terraform apply
# Run: terraform output

output "cloud_sql_instance_name" {
  description = "Cloud SQL instance name"
  value       = google_sql_database_instance.main.name
}

output "cloud_sql_private_ip" {
  description = "Cloud SQL private IP — only reachable inside VPC"
  value       = google_sql_database_instance.main.private_ip_address
}

output "artifact_registry_url" {
  description = "Docker image registry URL"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${google_artifact_registry_repository.healthcare.repository_id}"
}

output "redis_host" {
  description = "Memorystore Redis host (VPC-internal)"
  value       = google_redis_instance.cache.host
}

output "vpc_connector_id" {
  description = "Serverless VPC Access Connector ID — used in gcloud run deploy"
  value       = google_vpc_access_connector.connector.id
}

output "cloud_run_sa_email" {
  description = "Cloud Run runtime service account email"
  value       = google_service_account.cloud_run_sa.email
}

output "wif_provider" {
  description = "Workload Identity Federation provider — use in GitHub Actions WIF_PROVIDER secret"
  value       = google_iam_workload_identity_pool_provider.github_provider.name
}

output "github_actions_sa" {
  description = "GitHub Actions service account email — use in GitHub Actions WIF_SERVICE_ACCOUNT secret"
  value       = google_service_account.github_actions.email
}
