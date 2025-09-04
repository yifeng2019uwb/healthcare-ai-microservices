# User Profiles table - Core user profile data (no authentication)
# Industry standard: Common fields only, role-specific data in separate tables

# Create ENUM types first
resource "postgresql_extension" "uuid_ossp" {
  provider = postgresql.neon
  name     = "uuid-ossp"
}

# Create custom ENUM types
resource "postgresql_schema" "public" {
  provider = postgresql.neon
  name     = "public"
}

# Create ENUM types
resource "postgresql_sql" "gender_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN');"
}

resource "postgresql_sql" "role_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE role_enum AS ENUM ('PATIENT', 'PROVIDER');"
}

resource "postgresql_sql" "status_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');"
}

resource "postgresql_table" "user_profiles" {
  provider = postgresql.neon
  name     = "user_profiles"
  schema   = postgresql_schema.public.name

  depends_on = [
    postgresql_sql.gender_enum,
    postgresql_sql.role_enum,
    postgresql_sql.status_enum
  ]

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "external_auth_id"
    type     = "VARCHAR(255)"
    null_able = false
    comment  = "External authentication provider ID (Auth0, Cognito, etc). Future: rename to auth_id if internal auth is added"
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
    name     = "email"
    type     = "VARCHAR(255)"
    null_able = false
  }

  column {
    name     = "phone"
    type     = "VARCHAR(20)"
    null_able = false
  }

  column {
    name     = "date_of_birth"
    type     = "DATE"
    null_able = false
  }

  column {
    name     = "gender"
    type     = "gender_enum"
    null_able = false
  }

  column {
    name     = "street_address"
    type     = "VARCHAR(255)"
    null_able = true
  }

  column {
    name     = "city"
    type     = "VARCHAR(100)"
    null_able = true
  }

  column {
    name     = "state"
    type     = "VARCHAR(50)"
    null_able = true
  }

  column {
    name     = "postal_code"
    type     = "VARCHAR(20)"
    null_able = true
  }

  column {
    name     = "country"
    type     = "VARCHAR(50)"
    null_able = true
  }

  column {
    name     = "role"
    type     = "role_enum"
    null_able = false
  }

  column {
    name     = "status"
    type     = "status_enum"
    null_able = false
    default  = "ACTIVE"
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
    type     = "VARCHAR(100)"
    null_able = true
  }

  primary_key {
    columns = ["id"]
  }
}

# Create indexes
resource "postgresql_index" "user_profiles_external_auth_id_unique" {
  provider = postgresql.neon
  name     = "idx_user_profiles_external_auth_id_unique"
  table    = postgresql_table.user_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["external_auth_id"]
  unique   = true
}

resource "postgresql_index" "user_profiles_email_unique" {
  provider = postgresql.neon
  name     = "idx_user_profiles_email_unique"
  table    = postgresql_table.user_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["email"]
  unique   = true
}

resource "postgresql_index" "user_profiles_phone" {
  provider = postgresql.neon
  name     = "idx_user_profiles_phone"
  table    = postgresql_table.user_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["phone"]
}

resource "postgresql_index" "user_profiles_name_dob" {
  provider = postgresql.neon
  name     = "idx_user_profiles_name_dob"
  table    = postgresql_table.user_profiles.name
  schema   = postgresql_schema.public.name
  columns  = ["last_name", "first_name", "date_of_birth"]
}
