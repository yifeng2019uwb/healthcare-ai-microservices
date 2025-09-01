# Providers table - Healthcare provider profiles and credentials
resource "postgresql_table" "providers" {
  provider = postgresql.neon
  name     = "providers"
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
    name     = "specialty"
    type     = "VARCHAR(100)"
    null_able = false
  }

  column {
    name     = "license_number"
    type     = "VARCHAR(50)"
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
    name     = "credentials"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "bio"
    type     = "TEXT"
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
      table  = postgresql_table.users.name
      column = "id"
    }
  }
}

# Create index on user_id for faster lookups
resource "postgresql_index" "providers_user_id" {
  provider = postgresql.neon
  name     = "idx_providers_user_id"
  table    = postgresql_table.providers.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
}

# Create index on specialty for provider search
resource "postgresql_index" "providers_specialty" {
  provider = postgresql.neon
  name     = "idx_providers_specialty"
  table    = postgresql_table.providers.name
  schema   = postgresql_schema.public.name
  columns  = ["specialty"]
}
