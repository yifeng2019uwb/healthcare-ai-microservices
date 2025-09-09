# Appointments Table
# This file creates the appointments table and related ENUMs using null_resource

resource "null_resource" "create_appointments_table" {
  depends_on = [null_resource.create_patient_profiles_table, null_resource.create_provider_profiles_table]
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.supabase_password}" psql \
        -h "${var.supabase_host}" \
        -p "${var.supabase_port}" \
        -U "${var.supabase_username}" \
        -d "${var.supabase_database}" \
        -c "
      -- Note: Using VARCHAR with CHECK constraints instead of ENUM types
      -- This provides better compatibility with Hibernate when ddl-auto: none

      -- Create appointments table
      CREATE TABLE IF NOT EXISTS appointments (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          patient_id UUID REFERENCES patient_profiles(id) ON DELETE CASCADE,
          provider_id UUID NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
          scheduled_at TIMESTAMPTZ NOT NULL,
          checkin_time TIMESTAMPTZ,
          status VARCHAR(20) NOT NULL CHECK (status IN ('AVAILABLE', 'SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
          appointment_type VARCHAR(30) NOT NULL CHECK (appointment_type IN ('REGULAR_CONSULTATION', 'FOLLOW_UP', 'NEW_PATIENT_INTAKE', 'PROCEDURE_CONSULTATION')),
          notes TEXT,
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(255)
      );

      -- Create indexes for appointments
      CREATE INDEX IF NOT EXISTS idx_provider_schedule ON appointments(provider_id, status, scheduled_at);
      CREATE INDEX IF NOT EXISTS idx_patient_schedule ON appointments(patient_id, scheduled_at DESC);
      "
    EOT
  }
  triggers = {
    schema_version = "2.0"
  }
}
