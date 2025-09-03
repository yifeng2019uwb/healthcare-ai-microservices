# Appointments table - Appointment scheduling and management

# Create ENUM types for appointments
resource "postgresql_sql" "appointment_status_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE appointment_status_enum AS ENUM ('AVAILABLE', 'SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');"
}

resource "postgresql_sql" "appointment_type_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE appointment_type_enum AS ENUM ('REGULAR_CONSULTATION', 'FOLLOW_UP', 'NEW_PATIENT_INTAKE', 'PROCEDURE_CONSULTATION');"
}

resource "postgresql_table" "appointments" {
  provider = postgresql.neon
  name     = "appointments"
  schema   = postgresql_schema.public.name

  depends_on = [
    postgresql_sql.appointment_status_enum,
    postgresql_sql.appointment_type_enum
  ]

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "patient_id"
    type     = "UUID"
    null_able = true
  }

  column {
    name     = "provider_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "scheduled_at"
    type     = "TIMESTAMPTZ"
    null_able = false
  }

  column {
    name     = "checkin_time"
    type     = "TIMESTAMPTZ"
    null_able = true
  }

  column {
    name     = "status"
    type     = "appointment_status_enum"
    null_able = false
    default  = "AVAILABLE"
  }

  column {
    name     = "appointment_type"
    type     = "appointment_type_enum"
    null_able = false
  }

  column {
    name     = "notes"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "custom_data"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "created_at"
    type     = "TIMESTAMPTZ"
    null_able = false
    default  = "CURRENT_TIMESTAMP"
  }

  column {
    name     = "updated_at"
    type     = "TIMESTAMPTZ"
    null_able = false
    default  = "CURRENT_TIMESTAMP"
  }

  column {
    name     = "updated_by"
    type     = "VARCHAR(255)"
    null_able = true
  }

  primary_key {
    columns = ["id"]
  }

  foreign_key {
    columns     = ["patient_id"]
    references {
      table  = postgresql_table.patient_profiles.name
      column = "id"
    }
  }

  foreign_key {
    columns     = ["provider_id"]
    references {
      table  = postgresql_table.provider_profiles.name
      column = "id"
    }
  }
}

# For provider calendar & conflict checks
resource "postgresql_index" "appointments_provider_schedule" {
  provider = postgresql.neon
  name     = "idx_provider_schedule"
  table    = postgresql_table.appointments.name
  schema   = postgresql_schema.public.name
  columns  = ["provider_id", "status", "scheduled_at"]
}

# For patient history (DESC order for most recent first)
resource "postgresql_index" "appointments_patient_schedule" {
  provider = postgresql.neon
  name     = "idx_patient_schedule"
  table    = postgresql_table.appointments.name
  schema   = postgresql_schema.public.name
  columns  = ["patient_id", "scheduled_at"]

  # Note: DESC ordering is handled at query level for better performance
  # The index supports both ASC and DESC queries efficiently
}

# Create index on updated_by
resource "postgresql_index" "appointments_updated_by" {
  provider = postgresql.neon
  name     = "idx_appointments_updated_by"
  table    = postgresql_table.appointments.name
  schema   = postgresql_schema.public.name
  columns  = ["updated_by"]
}