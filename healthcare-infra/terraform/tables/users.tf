# User Profiles table - Core user profile data (no authentication)
# Industry standard: Common fields only, role-specific data in separate tables
resource "postgresql_table" "user_profiles" {
  provider = postgresql.neon
  name     = "user_profiles"
  schema   = postgresql_schema.public.name

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "external_user_id"
    type     = "VARCHAR(255)"
    null_able = false
  }

  column {
    name     = "email"
    type     = "VARCHAR(255)"
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
    null_able = true
  }

  column {
    name     = "phone"
    type     = "VARCHAR(20)"
    null_able = true
  }

  column {
    name     = "gender"
    type     = "VARCHAR(10)"
    null_able = true
  }

  column {
    name     = "address"
    type     = "VARCHAR(500)"
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
    name     = "role"
    type     = "VARCHAR(20)"
    null_able = false
  }

  column {
    name     = "status"
    type     = "VARCHAR(20)"
    null_able = false
    default  = "ACTIVE"
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
resource "postgresql_index" "user_profiles_email_unique" {
  provider = postgresql.neon
  name     = "idx_user_profiles_email_unique"
  table    = postgresql_table.user_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["email"]
  unique   = true
}
