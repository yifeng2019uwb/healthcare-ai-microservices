# Medical Records Table
# This file creates the medical_records table and related ENUMs using null_resource

resource "null_resource" "create_medical_records_table" {
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.neon_password}" psql \
        -h "${var.neon_host}" \
        -p "${var.neon_port}" \
        -U "${var.neon_username}" \
        -d "${var.neon_database}" \
        -c "
      -- Create ENUM types for medical records
      DO \$\$ BEGIN
          CREATE TYPE medical_record_type_enum AS ENUM ('CONSULTATION', 'DIAGNOSIS', 'TREATMENT', 'PRESCRIPTION', 'LAB_RESULT', 'IMAGING', 'VITAL_SIGNS', 'PROGRESS_NOTE', 'DISCHARGE_SUMMARY', 'EMERGENCY_NOTE');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;
      
      DO \$\$ BEGIN
          CREATE TYPE medical_record_status_enum AS ENUM ('DRAFT', 'FINALIZED', 'ARCHIVED', 'AMENDED');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;
      
      DO \$\$ BEGIN
          CREATE TYPE medical_record_priority_enum AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT', 'CRITICAL');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create medical_records table
      CREATE TABLE IF NOT EXISTS medical_records (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          record_number VARCHAR(20) NOT NULL UNIQUE,
          patient_id UUID NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
          provider_id UUID NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
          appointment_id UUID REFERENCES appointments(id) ON DELETE SET NULL,
          record_type medical_record_type_enum NOT NULL,
          status medical_record_status_enum NOT NULL DEFAULT 'DRAFT',
          priority medical_record_priority_enum NOT NULL DEFAULT 'MEDIUM',
          title VARCHAR(255) NOT NULL,
          content TEXT NOT NULL,
          diagnosis_codes TEXT[],
          procedure_codes TEXT[],
          medications TEXT[],
          vital_signs JSONB,
          lab_results JSONB,
          imaging_results JSONB,
          attachments JSONB,
          is_confidential BOOLEAN DEFAULT FALSE,
          requires_review BOOLEAN DEFAULT FALSE,
          reviewed_by UUID REFERENCES user_profiles(id),
          reviewed_at TIMESTAMPTZ,
          finalized_at TIMESTAMPTZ,
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for medical_records
      CREATE UNIQUE INDEX IF NOT EXISTS idx_medical_records_record_number_unique ON medical_records(record_number);
      CREATE INDEX IF NOT EXISTS idx_medical_records_patient_id ON medical_records(patient_id);
      CREATE INDEX IF NOT EXISTS idx_medical_records_provider_id ON medical_records(provider_id);
      CREATE INDEX IF NOT EXISTS idx_medical_records_appointment_id ON medical_records(appointment_id);
      CREATE INDEX IF NOT EXISTS idx_medical_records_record_type ON medical_records(record_type);
      CREATE INDEX IF NOT EXISTS idx_medical_records_status ON medical_records(status);
      CREATE INDEX IF NOT EXISTS idx_medical_records_priority ON medical_records(priority);
      CREATE INDEX IF NOT EXISTS idx_medical_records_is_confidential ON medical_records(is_confidential);
      CREATE INDEX IF NOT EXISTS idx_medical_records_requires_review ON medical_records(requires_review);
      CREATE INDEX IF NOT EXISTS idx_medical_records_reviewed_by ON medical_records(reviewed_by);
      CREATE INDEX IF NOT EXISTS idx_medical_records_finalized_at ON medical_records(finalized_at);
      CREATE INDEX IF NOT EXISTS idx_medical_records_created_at ON medical_records(created_at);
      "
    EOT
  }
  triggers = {
    schema_version = "1.0"
  }
}
