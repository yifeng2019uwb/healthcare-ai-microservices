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

# Note: Database tables are now managed by Supabase
# See healthcare-infra/terraform/supabase/ for Supabase-specific configurations
