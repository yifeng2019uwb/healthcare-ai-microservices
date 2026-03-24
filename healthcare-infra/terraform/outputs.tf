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

output "gateway_url" {
  description = "Gateway Cloud Run public URL"
  value       = google_cloud_run_v2_service.gateway.uri
}

output "patient_service_url" {
  description = "Patient service Cloud Run URL (internal only)"
  value       = google_cloud_run_v2_service.patient_service.uri
}

output "appointment_service_url" {
  description = "Appointment service Cloud Run URL (internal only)"
  value       = google_cloud_run_v2_service.appointment_service.uri
}

output "wif_provider" {
  description = "Workload Identity Federation provider — use in GitHub Actions WIF_PROVIDER secret"
  value       = google_iam_workload_identity_pool_provider.github_provider.name
}

output "github_actions_sa" {
  description = "GitHub Actions service account email — use in GitHub Actions WIF_SERVICE_ACCOUNT secret"
  value       = google_service_account.github_actions.email
}