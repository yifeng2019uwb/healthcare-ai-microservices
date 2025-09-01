# Appointments table - Appointment scheduling and management
resource "postgresql_table" "appointments" {
  provider = postgresql.neon
  name     = "appointments"
  schema   = postgresql_schema.public.name

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "patient_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "provider_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "appointment_date"
    type     = "DATE"
    null_able = false
  }

  column {
    name     = "start_time"
    type     = "TIME"
    null_able = false
  }

  column {
    name     = "end_time"
    type     = "TIME"
    null_able = false
  }

  column {
    name     = "status"
    type     = "VARCHAR(20)"
    null_able = false
    default  = "SCHEDULED"
  }

  column {
    name     = "appointment_type"
    type     = "VARCHAR(50)"
    null_able = true
  }

  column {
    name     = "notes"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "created_at"
    type     = "TIMESTAMP"
    null_able = false
    default  = "CURRENT_TIMESTAMP"
  }

  column {
    name     = "updated_at"
    type     = "TIMESTAMP"
    null_able = false
    default  = "CURRENT_TIMESTAMP"
  }

  primary_key {
    columns = ["id"]
  }

  foreign_key {
    columns     = ["patient_id"]
    references {
      table  = postgresql_table.patients.name
      column = "id"
    }
  }

  foreign_key {
    columns     = ["provider_id"]
    references {
      table  = postgresql_table.providers.name
      column = "id"
    }
  }
}

# Create index on patient_id for faster lookups
resource "postgresql_index" "appointments_patient_id" {
  provider = postgresql.neon
  name     = "idx_appointments_patient_id"
  table    = postgresql_table.appointments.name
  schema   = postgresql_schema.public.name
  columns  = ["patient_id"]
}

# Create index on provider_id for faster lookups
resource "postgresql_index" "appointments_provider_id" {
  provider = postgresql.neon
  name     = "idx_appointments_provider_id"
  table    = postgresql_table.appointments.name
  schema   = postgresql_schema.public.name
  columns  = ["provider_id"]
}

# Create composite index for appointment scheduling queries
resource "postgresql_index" "appointments_provider_date" {
  provider = postgresql.neon
  name     = "idx_appointments_provider_date"
  table    = postgresql_table.appointments.name
  schema   = postgresql_schema.public.name
  columns  = ["provider_id", "appointment_date", "start_time"]
}
