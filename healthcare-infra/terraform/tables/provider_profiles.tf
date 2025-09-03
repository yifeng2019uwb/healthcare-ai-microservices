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
    name     = "specialty"
    type     = "VARCHAR(100)"
    null_able = true
  }

  column {
    name     = "license_number"
    type     = "VARCHAR(50)"
    null_able = true
  }

  column {
    name     = "qualification"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "bio"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "years_of_experience"
    type     = "INTEGER"
    null_able = true
  }

  column {
    name     = "office_address"
    type     = "VARCHAR(500)"
    null_able = true
  }

  column {
    name     = "office_phone"
    type     = "VARCHAR(20)"
    null_able = true
  }

  column {
    name     = "is_available"
    type     = "BOOLEAN"
    null_able = false
    default  = "true"
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

# Create unique index on license_number
resource "postgresql_index" "provider_profiles_license_number_unique" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_license_number_unique"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["license_number"]
  unique   = true
}

# Create index on user_id for faster lookups
resource "postgresql_index" "provider_profiles_user_id" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_user_id"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
}

# Create index on specialty for provider search
resource "postgresql_index" "provider_profiles_specialty" {
  provider = postgresql.neon
  name     = "idx_provider_profiles_specialty"
  table    = postgresql_table.provider_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["specialty"]
}
