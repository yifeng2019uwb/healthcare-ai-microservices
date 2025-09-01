# Medical records table - Patient medical records and documents
resource "postgresql_table" "medical_records" {
  provider = postgresql.neon
  name     = "medical_records"
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
    name     = "appointment_id"
    type     = "UUID"
    null_able = true
  }

  column {
    name     = "record_type"
    type     = "VARCHAR(50)"
    null_able = false
  }

  column {
    name     = "title"
    type     = "VARCHAR(255)"
    null_able = false
  }

  column {
    name     = "description"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "diagnosis"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "treatment"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "medications"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "vital_signs"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "file_urls"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "is_confidential"
    type     = "BOOLEAN"
    null_able = false
    default  = "false"
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

  foreign_key {
    columns     = ["appointment_id"]
    references {
      table  = postgresql_table.appointments.name
      column = "id"
    }
  }
}

# Create index on patient_id for faster lookups
resource "postgresql_index" "medical_records_patient_id" {
  provider = postgresql.neon
  name     = "idx_medical_records_patient_id"
  table    = postgresql_table.medical_records.name
  schema   = postgresql_schema.public.name
  columns  = ["patient_id"]
}

# Create index on provider_id for faster lookups
resource "postgresql_index" "medical_records_provider_id" {
  provider = postgresql.neon
  name     = "idx_medical_records_provider_id"
  table    = postgresql_table.medical_records.name
  schema   = postgresql_schema.public.name
  columns  = ["provider_id"]
}

# Create index on record_type for filtering
resource "postgresql_index" "medical_records_type" {
  provider = postgresql.neon
  name     = "idx_medical_records_type"
  table    = postgresql_table.medical_records.name
  schema   = postgresql_schema.public.name
  columns  = ["record_type"]
}
