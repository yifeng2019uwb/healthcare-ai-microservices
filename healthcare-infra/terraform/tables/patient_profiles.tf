# Patient Profiles Table
# This file creates the patient_profiles table and related ENUMs using null_resource

resource "null_resource" "create_patient_profiles_table" {
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.neon_password}" psql \
        -h "${var.neon_host}" \
        -p "${var.neon_port}" \
        -U "${var.neon_username}" \
        -d "${var.neon_database}" \
        -c "
      -- Create ENUM types for patient profiles
      DO \$\$ BEGIN
          CREATE TYPE patient_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'DECEASED');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE insurance_type_enum AS ENUM ('PRIVATE', 'MEDICARE', 'MEDICAID', 'TRICARE', 'OTHER');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE emergency_contact_relationship_enum AS ENUM ('SPOUSE', 'PARENT', 'CHILD', 'SIBLING', 'FRIEND', 'OTHER');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create patient_profiles table
      CREATE TABLE IF NOT EXISTS patient_profiles (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          user_profile_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
          patient_number VARCHAR(20) NOT NULL UNIQUE,
          date_of_birth DATE NOT NULL,
          gender gender_enum NOT NULL,
          phone VARCHAR(20) NOT NULL,
          email VARCHAR(255) NOT NULL,
          street_address VARCHAR(255),
          city VARCHAR(100),
          state VARCHAR(50),
          postal_code VARCHAR(20),
          country VARCHAR(50),
          emergency_contact_name VARCHAR(100),
          emergency_contact_phone VARCHAR(20),
          emergency_contact_relationship emergency_contact_relationship_enum,
          insurance_provider VARCHAR(100),
          insurance_policy_number VARCHAR(50),
          insurance_type insurance_type_enum,
          medical_conditions TEXT[],
          allergies TEXT[],
          medications TEXT[],
          status patient_status_enum NOT NULL DEFAULT 'ACTIVE',
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for patient_profiles
      CREATE UNIQUE INDEX IF NOT EXISTS idx_patient_profiles_patient_number_unique ON patient_profiles(patient_number);
      CREATE UNIQUE INDEX IF NOT EXISTS idx_patient_profiles_email_unique ON patient_profiles(email);
      CREATE INDEX IF NOT EXISTS idx_patient_profiles_phone ON patient_profiles(phone);
      CREATE INDEX IF NOT EXISTS idx_patient_profiles_user_profile_id ON patient_profiles(user_profile_id);
      CREATE INDEX IF NOT EXISTS idx_patient_profiles_dob ON patient_profiles(date_of_birth);
      CREATE INDEX IF NOT EXISTS idx_patient_profiles_status ON patient_profiles(status);
      CREATE INDEX IF NOT EXISTS idx_patient_profiles_insurance ON patient_profiles(insurance_provider, insurance_policy_number);
      "
    EOT
  }
  triggers = {
    schema_version = "1.0"
  }
}
