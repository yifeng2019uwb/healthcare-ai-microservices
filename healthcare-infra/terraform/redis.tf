# Cloud Memorystore Redis — JWT token blacklist (shared by auth-service and gateway)

resource "google_redis_instance" "cache" {
  name           = "healthcare-redis-${var.environment}"
  tier           = "BASIC"
  memory_size_gb = 1
  region         = var.region
  project        = var.project_id

  authorized_network = google_compute_network.vpc.id
  connect_mode       = "PRIVATE_SERVICE_ACCESS"

  depends_on = [
    google_service_networking_connection.private_vpc_connection,
    google_project_service.apis
  ]
}

# Serverless VPC Access Connector — allows Cloud Run to reach VPC resources (Redis, Cloud SQL)
resource "google_vpc_access_connector" "connector" {
  name          = "healthcare-connector"
  region        = var.region
  project       = var.project_id
  network       = google_compute_network.vpc.name
  ip_cidr_range = "10.8.0.0/28"

  depends_on = [google_project_service.apis]
}
