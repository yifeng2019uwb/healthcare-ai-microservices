# Configure Terraform providers
terraform {
  required_providers {
    neon = {
      source = "kislerdm/neon"
    }
    postgresql = {
      source  = "cyrilgdn/postgresql"
      version = "1.12.0"
    }
  }
}

# Configure the Neon provider using an environment variable for the API key
# The provider will read the NEON_API_KEY environment variable by default.
provider "neon" {}

# Configure the PostgreSQL provider for existing Neon database
provider "postgresql" {
  alias = "neon"
  host     = var.neon_host
  port     = var.neon_port
  database = var.neon_database
  username = var.neon_username
  password = var.neon_password
  sslmode  = "require"
}
