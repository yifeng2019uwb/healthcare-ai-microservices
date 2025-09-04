# Medical records table - Patient medical records and documents

# Create ENUM type for record_type
resource "postgresql_sql" "record_type_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE record_type_enum AS ENUM ('DIAGNOSIS', 'TREATMENT', 'SUMMARY', 'LAB_RESULT', 'PRESCRIPTION', 'NOTE', 'OTHER');"
}

resource "postgresql_table" "medical_records" {
  provider = postgresql.neon
  name     = "medical_records"
  schema   = postgresql_schema.public.name

  depends_on = [
    postgresql_sql.record_type_enum
  ]

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "appointment_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "record_type"
    type     = "record_type_enum"
    null_able = false
  }

  column {
    name     = "content"
    type     = "TEXT"
    null_able = false
  }

  column {
    name     = "is_patient_visible"
    type     = "BOOLEAN"
    null_able = false
    default  = "false"
  }

  column {
    name     = "release_date"
    type     = "TIMESTAMPTZ"
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
    type     = "VARCHAR(100)"
    null_able = true
  }

  primary_key {
    columns = ["id"]
  }

  foreign_key {
    columns     = ["appointment_id"]
    references {
      table  = postgresql_table.appointments.name
      column = "id"
    }
  }
}

# Create composite index for appointment_id + record_type
# Note: This composite index also efficiently handles queries on appointment_id alone
resource "postgresql_index" "medical_records_appointment_type" {
  provider = postgresql.neon
  name     = "idx_medical_records_appointment_type"
  table    = postgresql_table.medical_records.name
  schema   = postgresql_schema.public.name
  columns  = ["appointment_id", "record_type"]
}

# Patient-visible filter index for portal requests
# Efficient when serving portal requests (patients only see their released data)
resource "postgresql_index" "medical_records_patient_visible" {
  provider = postgresql.neon
  name     = "idx_medical_records_patient_visible"
  table    = postgresql_table.medical_records.name
  schema   = postgresql_schema.public.name
  columns  = ["appointment_id", "is_patient_visible", "release_date"]
}
