# Output values for the Neon database infrastructure
output "neon_project_id" {
  description = "Neon project ID"
  value       = "medconnect-healthcare"
}

output "neon_project_name" {
  description = "Neon project name"
  value       = "medconnect-healthcare"
}

# Development branch outputs
output "development_database_name" {
  description = "Development database name"
  value       = var.neon_database
}

output "development_database_host" {
  description = "Development database host"
  value       = var.neon_host
}

output "development_database_port" {
  description = "Development database port"
  value       = var.neon_port
}

output "development_database_url" {
  description = "Development database connection URL"
  value       = "postgresql://${var.neon_username}:${var.neon_password}@${var.neon_host}:${var.neon_port}/${var.neon_database}?sslmode=require"
  sensitive   = true
}


output "database_tables" {
  description = "List of created database tables"
  value = [
    "user_profiles",
    "patient_profiles",
    "provider_profiles",
    "appointments",
    "medical_records",
    "audit_logs"
  ]
}

output "database_enums" {
  description = "List of created database ENUMs"
  value = [
    "gender_enum",
    "role_enum",
    "status_enum",
    "patient_status_enum",
    "insurance_type_enum",
    "emergency_contact_relationship_enum",
    "provider_type_enum",
    "provider_status_enum",
    "license_status_enum",
    "appointment_status_enum",
    "appointment_type_enum",
    "appointment_priority_enum",
    "medical_record_type_enum",
    "medical_record_status_enum",
    "medical_record_priority_enum",
    "audit_action_enum",
    "audit_status_enum",
    "resource_type_enum"
  ]
}
