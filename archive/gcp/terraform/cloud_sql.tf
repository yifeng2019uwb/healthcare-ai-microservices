# Cloud SQL — PostgreSQL 15
# Private IP only — accessible inside VPC via Cloud SQL Auth Proxy locally

resource "google_sql_database_instance" "main" {
  name             = "healthcare-db-${var.environment}"
  database_version = "POSTGRES_15"
  region           = var.region
  project          = var.project_id

  settings {
    tier = "db-f1-micro"

    ip_configuration {
      ipv4_enabled                                  = var.environment == "dev" ? true : false

      private_network                               = google_compute_network.vpc.id
      enable_private_path_for_google_cloud_services = true
    }

    backup_configuration {
      enabled = false  # dev environment — enable for prod
    }

    disk_size = 10
    disk_type = "PD_SSD"
  }

  deletion_protection = false  # dev — set true for prod

  depends_on = [google_service_networking_connection.private_vpc_connection]
}

resource "google_sql_database" "healthcare" {
  name     = "healthcare"
  instance = google_sql_database_instance.main.name
  project  = var.project_id
}

resource "google_sql_user" "postgres" {
  name     = "postgres"
  instance = google_sql_database_instance.main.name
  password = var.db_password
  project  = var.project_id
}