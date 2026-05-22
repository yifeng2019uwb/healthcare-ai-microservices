# Secret Manager — all credentials stored here
# Values are set manually after creation — never in code
# Cloud Run services access secrets at runtime via --set-secrets flag

resource "google_secret_manager_secret" "db_password" {
  secret_id = "db-password"
  project   = var.project_id

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "firebase_project_id" {
  secret_id = "firebase-project-id"
  project   = var.project_id

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "firebase_service_account" {
  secret_id = "firebase-service-account"
  project   = var.project_id

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "jwt_secret" {
  secret_id = "jwt-secret"
  project   = var.project_id

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "db_password_auth_service" {
  secret_id = "db-password-auth-service"
  project   = var.project_id
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "db_password_patient_service" {
  secret_id = "db-password-patient-service"
  project   = var.project_id
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "db_password_provider_service" {
  secret_id = "db-password-provider-service"
  project   = var.project_id
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "db_password_appointment_service" {
  secret_id = "db-password-appointment-service"
  project   = var.project_id
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "jwt_private_key" {
  secret_id = "jwt-private-key"
  project   = var.project_id
  replication {
    auto {}
  }
}

resource "google_secret_manager_secret" "jwt_public_key" {
  secret_id = "jwt-public-key"
  project   = var.project_id
  replication {
    auto {}
  }
}

