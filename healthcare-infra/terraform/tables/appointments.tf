# Appointments Table
# This file creates the appointments table and related ENUMs using null_resource

resource "null_resource" "create_appointments_table" {
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.neon_password}" psql \
        -h "${var.neon_host}" \
        -p "${var.neon_port}" \
        -U "${var.neon_username}" \
        -d "${var.neon_database}" \
        -c "
      -- Create ENUM types for appointments
      DO \$\$ BEGIN
          CREATE TYPE appointment_status_enum AS ENUM ('SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW', 'RESCHEDULED');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE appointment_type_enum AS ENUM ('CONSULTATION', 'FOLLOW_UP', 'EMERGENCY', 'ROUTINE_CHECKUP', 'SPECIALIST', 'THERAPY', 'DIAGNOSTIC', 'TREATMENT');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE appointment_priority_enum AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT', 'EMERGENCY');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create appointments table
      CREATE TABLE IF NOT EXISTS appointments (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          appointment_number VARCHAR(20) NOT NULL UNIQUE,
          patient_id UUID NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
          provider_id UUID NOT NULL REFERENCES provider_profiles(id) ON DELETE CASCADE,
          appointment_date DATE NOT NULL,
          appointment_time TIME NOT NULL,
          duration_minutes INTEGER NOT NULL DEFAULT 30,
          appointment_type appointment_type_enum NOT NULL,
          status appointment_status_enum NOT NULL DEFAULT 'SCHEDULED',
          priority appointment_priority_enum NOT NULL DEFAULT 'MEDIUM',
          reason TEXT,
          notes TEXT,
          location VARCHAR(255),
          room_number VARCHAR(20),
          reminder_sent BOOLEAN DEFAULT FALSE,
          reminder_sent_at TIMESTAMPTZ,
          cancelled_at TIMESTAMPTZ,
          cancelled_by UUID REFERENCES user_profiles(id),
          cancellation_reason TEXT,
          rescheduled_from UUID REFERENCES appointments(id),
          custom_data JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
          updated_by VARCHAR(100)
      );

      -- Create indexes for appointments
      CREATE UNIQUE INDEX IF NOT EXISTS idx_appointments_appointment_number_unique ON appointments(appointment_number);
      CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
      CREATE INDEX IF NOT EXISTS idx_appointments_provider_id ON appointments(provider_id);
      CREATE INDEX IF NOT EXISTS idx_appointments_appointment_date ON appointments(appointment_date);
      CREATE INDEX IF NOT EXISTS idx_appointments_appointment_datetime ON appointments(appointment_date, appointment_time);
      CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);
      CREATE INDEX IF NOT EXISTS idx_appointments_type ON appointments(appointment_type);
      CREATE INDEX IF NOT EXISTS idx_appointments_priority ON appointments(priority);
      CREATE INDEX IF NOT EXISTS idx_appointments_reminder_sent ON appointments(reminder_sent);
      CREATE INDEX IF NOT EXISTS idx_appointments_cancelled_at ON appointments(cancelled_at);
      CREATE INDEX IF NOT EXISTS idx_appointments_rescheduled_from ON appointments(rescheduled_from);
      "
    EOT
  }
  triggers = {
    schema_version = "1.0"
  }
}
