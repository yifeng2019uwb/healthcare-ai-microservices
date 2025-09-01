# Patients table - Patient profile and medical information
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
    name     = "user_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "first_name"
    type     = "VARCHAR(100)"
    null_able = false
  }

  column {
    name     = "last_name"
    type     = "VARCHAR(100)"
    null_able = false
  }

  column {
    name     = "date_of_birth"
    type     = "DATE"
    null_able = false
  }

  column {
    name     = "phone"
    type     = "VARCHAR(20)"
    null_able = true
  }

  column {
    name     = "address"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "emergency_contact"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "medical_history"
    type     = "JSONB"
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
    columns     = ["user_id"]
    references {
      table  = postgresql_table.users.name
      column = "id"
    }
  }
}

# Create index on user_id for faster lookups
resource "postgresql_index" "patients_user_id" {
  provider = postgresql.neon
  name     = "idx_patients_user_id"
  table    = postgresql_table.patients.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
}
