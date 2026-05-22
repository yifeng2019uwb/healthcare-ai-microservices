# Medical Records Table
# This file creates the medical_records table and related ENUMs using null_resource

resource "null_resource" "create_medical_records_table" {
  depends_on = [null_resource.create_appointments_table]
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.supabase_password}" psql \
        -h "${var.supabase_host}" \
        -p "${var.supabase_port}" \
        -U "${var.supabase_username}" \
        -d "${var.supabase_database}" \
        -c "
      -- Create ENUM types for medical records
      DO \$\$ BEGIN
          CREATE TYPE medical_record_type_enum AS ENUM ('DIAGNOSIS', 'TREATMENT', 'SUMMARY', 'LAB_RESULT', 'PRESCRIPTION', 'NOTE', 'OTHER');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create medical_records table
      CREATE TABLE IF NOT EXISTS medical_records (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          appointment_id UUID NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
          record_type medical_record_type_enum NOT NULL,
          content TEXT NOT NULL,
          is_patient_visible BOOLEAN NOT NULL,
          release_date TIMESTAMPTZ,
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for medical_records
      CREATE INDEX IF NOT EXISTS idx_medical_record_appointment_type ON medical_records(appointment_id, record_type);
      CREATE INDEX IF NOT EXISTS idx_medical_records_patient_visible ON medical_records(appointment_id, is_patient_visible, release_date);
      "
    EOT
  }
  triggers = {
    schema_version = "2.0"
  }
}
