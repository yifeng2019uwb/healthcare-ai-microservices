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
    name     = "patient_number"
    type     = "VARCHAR(50)"
    null_able = false
  }

  column {
    name     = "medical_history"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "allergies"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "current_medications"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "insurance_provider"
    type     = "VARCHAR(100)"
    null_able = true
  }

  column {
    name     = "insurance_policy_number"
    type     = "VARCHAR(50)"
    null_able = true
  }

  column {
    name     = "emergency_contact_name"
    type     = "VARCHAR(100)"
    null_able = true
  }

  column {
    name     = "emergency_contact_phone"
    type     = "VARCHAR(20)"
    null_able = true
  }

  column {
    name     = "primary_care_physician"
    type     = "VARCHAR(100)"
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
    columns     = ["user_id"]
    references {
      table  = postgresql_table.user_profiles.name
      column = "id"
    }
  }
}

# Create unique index on user_id
resource "postgresql_index" "patient_profiles_user_id_unique" {
  provider = postgresql.neon
  name     = "idx_patient_profiles_user_id_unique"
  table    = postgresql_table.patient_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
  unique   = true
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

# Create index on updated_by
resource "postgresql_index" "patient_profiles_updated_by" {
  provider = postgresql.neon
  name     = "idx_patient_profiles_updated_by"
  table    = postgresql_table.patient_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["updated_by"]
}