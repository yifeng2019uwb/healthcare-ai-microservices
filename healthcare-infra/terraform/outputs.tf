# Output values for the database infrastructure
output "neon_database_name" {
  description = "The Neon database name"
  value       = var.neon_database
}

output "neon_database_host" {
  description = "The Neon database host"
  value       = var.neon_host
}

output "neon_database_port" {
  description = "The Neon database port"
  value       = var.neon_port
}

output "neon_database_url" {
  description = "The complete database connection URL"
  value       = "postgresql://${var.neon_username}:${var.neon_password}@${var.neon_host}:${var.neon_port}/${var.neon_database}?sslmode=require"
  sensitive   = true
}

output "database_tables" {
  description = "List of created database tables"
  value = [
    postgresql_table.users.name,
    postgresql_table.patients.name,
    postgresql_table.providers.name,
    postgresql_table.appointments.name,
    postgresql_table.medical_records.name,
    postgresql_table.audit_logs.name
  ]
}
