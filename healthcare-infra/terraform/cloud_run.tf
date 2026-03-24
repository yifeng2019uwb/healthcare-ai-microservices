# Cloud Run Services — Phase 1
# gateway: public facing
# patient-service: internal only
# appointment-service: internal only
#
# Uses placeholder image initially — replaced by CI/CD on first deploy

locals {
  placeholder_image = "us-docker.pkg.dev/cloudrun/container/hello"
}

# =============================================================================
# Gateway
# =============================================================================
resource "google_cloud_run_v2_service" "gateway" {
  name     = "gateway-${var.environment}"
  location = var.region
  project  = var.project_id

  template {
    service_account = google_service_account.cloud_run_sa.email

    scaling {
      min_instance_count = 0
      max_instance_count = 3
    }

    containers {
      image = local.placeholder_image

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      env {
        name  = "ENVIRONMENT"
        value = var.environment
      }

      env {
        name = "FIREBASE_SA"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.firebase_service_account.secret_id
            version = "latest"
          }
        }
      }

      env {
        name = "JWT_SECRET"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.jwt_secret.secret_id
            version = "latest"
          }
        }
      }
    }
  }

  depends_on = [
    google_project_iam_member.cloud_run_sa_secret
  ]
}

# Public access — gateway is the only public-facing service
resource "google_cloud_run_v2_service_iam_member" "gateway_public" {
  project  = var.project_id
  location = var.region
  name     = google_cloud_run_v2_service.gateway.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}

# =============================================================================
# Patient Service — internal only
# =============================================================================
resource "google_cloud_run_v2_service" "patient_service" {
  name     = "patient-service-${var.environment}"
  location = var.region
  project  = var.project_id

  template {
    service_account = google_service_account.cloud_run_sa.email

    scaling {
      min_instance_count = 0
      max_instance_count = 3
    }

    containers {
      image = local.placeholder_image

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      env {
        name  = "ENVIRONMENT"
        value = var.environment
      }

      env {
        name = "DB_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
    }
  }

  depends_on = [
    google_project_iam_member.cloud_run_sa_secret
  ]
}

# =============================================================================
# Appointment Service — internal only
# =============================================================================
resource "google_cloud_run_v2_service" "appointment_service" {
  name     = "appointment-service-${var.environment}"
  location = var.region
  project  = var.project_id

  template {
    service_account = google_service_account.cloud_run_sa.email

    scaling {
      min_instance_count = 0
      max_instance_count = 3
    }

    containers {
      image = local.placeholder_image

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      env {
        name  = "ENVIRONMENT"
        value = var.environment
      }

      env {
        name = "DB_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
    }
  }

  depends_on = [
    google_project_iam_member.cloud_run_sa_secret
  ]
}