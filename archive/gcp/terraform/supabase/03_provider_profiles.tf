# Provider Profiles Table
# This file creates the provider_profiles table and related ENUMs using null_resource

resource "null_resource" "create_provider_profiles_table" {
  depends_on = [null_resource.create_database_schema]
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.supabase_password}" psql \
        -h "${var.supabase_host}" \
        -p "${var.supabase_port}" \
        -U "${var.supabase_username}" \
        -d "${var.supabase_database}" \
        -c "
      -- Create provider_profiles table
      CREATE TABLE IF NOT EXISTS provider_profiles (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          user_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
          license_numbers VARCHAR(50),
          npi_number VARCHAR(10) UNIQUE NOT NULL,
          specialty VARCHAR(100),
          qualifications TEXT,
          bio TEXT,
          office_phone VARCHAR(20),
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(255)
      );

      -- Create indexes for provider_profiles
      CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_profiles_user_id ON provider_profiles(user_id);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_provider_profiles_npi_number_unique ON provider_profiles(npi_number);
      CREATE INDEX IF NOT EXISTS idx_provider_profiles_specialty ON provider_profiles(specialty);
      "
    EOT
  }
  triggers = {
    schema_version = "2.0"
  }
}
