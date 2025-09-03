# Patients table - Visit and medical record tracking
# Industry standard: Separate table for visit/medical records, not profile data
resource "postgresql_table" "patients" {
  provider = postgresql.neon
  name     = "patients"
  schema   = postgresql_schema.public.name

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "patient_profile_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "appointment_ids"
    type     = "UUID[]"
    null_able = true
  }

  column {
    name     = "medical_record_ids"
    type     = "UUID[]"
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
    columns     = ["patient_profile_id"]
    references {
      table  = postgresql_table.patient_profiles.name
      column = "id"
    }
  }
}

# Create index on patient_profile_id for faster lookups
resource "postgresql_index" "patients_patient_profile_id" {
  provider = postgresql.neon
  name     = "idx_patients_patient_profile_id"
  table    = postgresql_table.patients.name
  schema   = postgresql_schema.public.name
  columns  = ["patient_profile_id"]
}
