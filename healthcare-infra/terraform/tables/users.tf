# Database Schema Deployment for Healthcare AI Microservices
# This file creates all database tables, enums, and indexes using the proper Neon approach

# Create all database objects using null_resource with local-exec provisioner
# Using existing neondb database directly to avoid API permission issues
resource "null_resource" "create_database_schema" {
  provisioner "local-exec" {
    command = <<-EOT
      # Get connection details from terraform.tfvars
      PGPASSWORD="${var.neon_password}" psql \
        -h "${var.neon_host}" \
        -p "${var.neon_port}" \
        -U "${var.neon_username}" \
        -d "${var.neon_database}" \
        -c "
      -- Enable UUID extension
      CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";

      -- Create ENUM types
      DO \$\$ BEGIN
          CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE role_enum AS ENUM ('PATIENT', 'PROVIDER');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create user_profiles table
      CREATE TABLE IF NOT EXISTS user_profiles (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          external_auth_id VARCHAR(255) NOT NULL,
          first_name VARCHAR(100) NOT NULL,
          last_name VARCHAR(100) NOT NULL,
          email VARCHAR(255) NOT NULL,
          phone VARCHAR(20) NOT NULL,
          date_of_birth DATE NOT NULL,
          gender gender_enum NOT NULL,
          street_address VARCHAR(255),
          city VARCHAR(100),
          state VARCHAR(50),
          postal_code VARCHAR(20),
          country VARCHAR(50),
          role role_enum NOT NULL,
          status status_enum NOT NULL DEFAULT 'ACTIVE',
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for user_profiles
      CREATE UNIQUE INDEX IF NOT EXISTS idx_user_profiles_external_auth_id_unique ON user_profiles(external_auth_id);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_user_profiles_email_unique ON user_profiles(email);
      CREATE INDEX IF NOT EXISTS idx_user_profiles_phone ON user_profiles(phone);
      CREATE INDEX IF NOT EXISTS idx_user_profiles_name_dob ON user_profiles(last_name, first_name, date_of_birth);
      "
    EOT
  }

  # Trigger recreation when SQL changes
  triggers = {
    schema_version = "1.0"
  }
}

# Create indexes
# Note: postgresql_index resource not supported in current provider version
# These indexes need to be created manually in the database
# resource "postgresql_index" "user_profiles_external_auth_id_unique" {
#   provider = postgresql.neon
#   name     = "idx_user_profiles_external_auth_id_unique"
#   table    = postgresql_table.user_profiles.name
#   schema   = postgresql_schema.public.name
#   columns  = ["external_auth_id"]
#   unique   = true
# }

# resource "postgresql_index" "user_profiles_email_unique" {
#   provider = postgresql.neon
#   name     = "idx_user_profiles_email_unique"
#   table    = postgresql_table.user_profiles.name
#   schema   = postgresql_schema.public.name
#   columns  = ["email"]
#   unique   = true
# }

# resource "postgresql_index" "user_profiles_phone" {
#   provider = postgresql.neon
#   name     = "idx_user_profiles_phone"
#   table    = postgresql_table.user_profiles.name
#   schema   = postgresql_schema.public.name
#   columns  = ["phone"]
# }

# resource "postgresql_index" "user_profiles_name_dob" {
#   provider = postgresql.neon
#   name     = "idx_user_profiles_name_dob"
#   table    = postgresql_table.user_profiles.name
#   schema   = postgresql_schema.public.name
#   columns  = ["last_name", "first_name", "date_of_birth"]
# }
