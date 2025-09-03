# Patient Profiles table - Patient-specific profile data
# Industry standard: Separate table for role-specific data
resource "postgresql_table" "patient_profiles" {
  provider = postgresql.neon
  name     = "patient_profiles"
  schema   = postgresql_schema.public.name

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "user_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "medical_history"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "allergies"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "patient_number"
    type     = "VARCHAR(50)"
    null_able = false
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
    columns     = ["user_id"]
    references {
      table  = postgresql_table.user_profiles.name
      column = "id"
    }
  }
}

# Create unique index on patient_number
resource "postgresql_index" "patient_profiles_patient_number_unique" {
  provider = postgresql.neon
  name     = "idx_patient_profiles_patient_number_unique"
  table    = postgresql_table.patient_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["patient_number"]
  unique   = true
}

# Create index on user_id for faster lookups
resource "postgresql_index" "patient_profiles_user_id" {
  provider = postgresql.neon
  name     = "idx_patient_profiles_user_id"
  table    = postgresql_table.patient_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
}
