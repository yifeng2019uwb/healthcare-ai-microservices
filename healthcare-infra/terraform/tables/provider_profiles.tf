# Provider Profiles table - Provider-specific profile data
# Industry standard: Separate table for role-specific data

resource "postgresql_table" "provider_profiles" {
  provider = postgresql.neon
  name     = "provider_profiles"
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
    name     = "license_numbers"
    type     = "VARCHAR(50)"
    null_able = true
  }

  column {
    name     = "npi_number"
    type     = "VARCHAR(10)"
    null_able = false
  }

  column {
    name     = "specialty"
    type     = "VARCHAR(100)"
    null_able = true
  }

  column {
    name     = "qualifications"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "bio"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "office_phone"
    type     = "VARCHAR(20)"
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
resource "postgresql_index" "provider_profiles_user_id_unique" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_user_id_unique"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
  unique   = true
}

# Create unique index on npi_number
resource "postgresql_index" "provider_profiles_npi_number_unique" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_npi_number_unique"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["npi_number"]
  unique   = true
}

# Create index on specialty for provider search
resource "postgresql_index" "provider_profiles_specialty" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_specialty"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["specialty"]
}

# Create index on updated_by
resource "postgresql_index" "provider_profiles_updated_by" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_updated_by"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["updated_by"]
}