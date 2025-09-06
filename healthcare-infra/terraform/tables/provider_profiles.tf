# Provider Profiles Table
# This file creates the provider_profiles table and related ENUMs using null_resource

resource "null_resource" "create_provider_profiles_table" {
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.neon_password}" psql \
        -h "${var.neon_host}" \
        -p "${var.neon_port}" \
        -U "${var.neon_username}" \
        -d "${var.neon_database}" \
        -c "
      -- Create ENUM types for provider profiles
      DO \$\$ BEGIN
          CREATE TYPE provider_type_enum AS ENUM ('DOCTOR', 'NURSE', 'SPECIALIST', 'THERAPIST', 'TECHNICIAN', 'ADMIN');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;
      
      DO \$\$ BEGIN
          CREATE TYPE provider_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'ON_LEAVE');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;
      
      DO \$\$ BEGIN
          CREATE TYPE license_status_enum AS ENUM ('ACTIVE', 'EXPIRED', 'SUSPENDED', 'REVOKED');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create provider_profiles table
      CREATE TABLE IF NOT EXISTS provider_profiles (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          user_profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
          provider_number VARCHAR(20) NOT NULL UNIQUE,
          npi_number VARCHAR(10) UNIQUE,
          license_number VARCHAR(50) NOT NULL,
          license_status license_status_enum NOT NULL DEFAULT 'ACTIVE',
          provider_type provider_type_enum NOT NULL,
          specialty VARCHAR(100),
          department VARCHAR(100),
          phone VARCHAR(20) NOT NULL,
          email VARCHAR(255) NOT NULL,
          street_address VARCHAR(255),
          city VARCHAR(100),
          state VARCHAR(50),
          postal_code VARCHAR(20),
          country VARCHAR(50),
          office_hours JSONB,
          languages TEXT[],
          certifications TEXT[],
          status provider_status_enum NOT NULL DEFAULT 'ACTIVE',
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for provider_profiles
      CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_profiles_provider_number_unique ON provider_profiles(provider_number);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_profiles_npi_number_unique ON provider_profiles(npi_number);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_profiles_license_number_unique ON provider_profiles(license_number);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_profiles_email_unique ON provider_profiles(email);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_phone ON provider_profiles(phone);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_user_profile_id ON provider_profiles(user_profile_id);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_provider_type ON provider_profiles(provider_type);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_specialty ON provider_profiles(specialty);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_department ON provider_profiles(department);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_status ON provider_profiles(status);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_license_status ON provider_profiles(license_status);
      "
    EOT
  }
  triggers = {
    schema_version = "1.0"
  }
}
