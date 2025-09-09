# Patient Profiles Table
# This file creates the patient_profiles table and related ENUMs using null_resource

resource "null_resource" "create_patient_profiles_table" {
  depends_on = [null_resource.create_database_schema]
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.supabase_password}" psql \
        -h "${var.supabase_host}" \
        -p "${var.supabase_port}" \
        -U "${var.supabase_username}" \
        -d "${var.supabase_database}" \
        -c "
      -- No additional ENUM types needed for patient_profiles

      -- Create patient_profiles table
      CREATE TABLE IF NOT EXISTS patient_profiles (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          user_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
          patient_number VARCHAR(50) NOT NULL UNIQUE,
          medical_history JSONB,
          allergies JSONB,
          current_medications TEXT,
          emergency_contact_name VARCHAR(100),
          emergency_contact_phone VARCHAR(20),
          insurance_provider VARCHAR(100),
          insurance_policy_number VARCHAR(50),
          primary_care_physician VARCHAR(100),
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for patient_profiles
      CREATE UNIQUE INDEX IF NOT EXISTS idx_patient_profiles_patient_number_unique ON patient_profiles(patient_number);
      CREATE INDEX IF NOT EXISTS idx_patient_profiles_user_id ON patient_profiles(user_id);
      "
    EOT
  }
  triggers = {
    schema_version = "2.0"
  }
}
