# Healthcare Infrastructure - Main Configuration
# This file manages the core infrastructure configuration

terraform {
  required_providers {
    null = {
      source  = "hashicorp/null"
      version = "3.2.1"
    }
  }
  required_version = ">= 1.0"
}

# Load database table configurations from tables/ directory
module "database_tables" {
  source = "./tables"

  # Pass variables to the tables module
  neon_host     = var.neon_host
  neon_port     = var.neon_port
  neon_database = var.neon_database
  neon_username = var.neon_username
  neon_password = var.neon_password
}
