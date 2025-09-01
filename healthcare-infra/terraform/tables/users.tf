# Users table - Core user authentication and profile data
resource "postgresql_table" "users" {
  provider = postgresql.neon
  name     = "users"
  schema   = postgresql_schema.public.name

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "email"
    type     = "VARCHAR(255)"
    null_able = false
  }

  column {
    name     = "password_hash"
    type     = "VARCHAR(255)"
    null_able = true
  }

  column {
    name     = "user_type"
    type     = "VARCHAR(20)"
    null_able = false
  }

  column {
    name     = "is_active"
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
}

# Create unique index on email
resource "postgresql_index" "users_email_unique" {
  provider = postgresql.neon
  name     = "idx_users_email_unique"
  table    = postgresql_table.users.name
  schema   = postgresql_schema.public.name
  columns  = ["email"]
  unique   = true
}
