# Audit logs table - Track all data changes for compliance and security

# Create ENUM types for audit logs
resource "postgresql_sql" "action_type_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE action_type_enum AS ENUM ('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT');"
}

resource "postgresql_sql" "resource_type_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE resource_type_enum AS ENUM ('USER_PROFILE', 'PATIENT_PROFILE', 'PROVIDER_PROFILE', 'APPOINTMENT', 'MEDICAL_RECORD');"
}

resource "postgresql_sql" "outcome_enum" {
  provider = postgresql.neon
  depends_on = [postgresql_schema.public]
  query = "CREATE TYPE outcome_enum AS ENUM ('SUCCESS', 'FAILURE');"
}

resource "postgresql_table" "audit_logs" {
  provider = postgresql.neon
  name     = "audit_logs"
  schema   = postgresql_schema.public.name

  depends_on = [
    postgresql_sql.action_type_enum,
    postgresql_sql.resource_type_enum,
    postgresql_sql.outcome_enum
  ]

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
    name     = "action_type"
    type     = "action_type_enum"
    null_able = false
  }

  column {
    name     = "resource_type"
    type     = "resource_type_enum"
    null_able = false
  }

  column {
    name     = "resource_id"
    type     = "UUID"
    null_able = true
  }

  column {
    name     = "outcome"
    type     = "outcome_enum"
    null_able = false
  }

  column {
    name     = "details"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "source_ip"
    type     = "INET"
    null_able = true
  }

  column {
    name     = "user_agent"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "created_at"
    type     = "TIMESTAMPTZ"
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

# Create optimized composite indexes for frequent query patterns
# Note: Individual field indexes removed to reduce write cost - composite indexes handle most queries efficiently

# For user activity queries: "show me this user's latest activity" or "last 100 records for user"
resource "postgresql_index" "audit_logs_user_activity" {
  provider = postgresql.neon
  name     = "idx_audit_logs_user_activity"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id", "created_at"]
  # Note: DESC ordering handled at query level for better performance
}

# For resource activity queries: "all activity for this resource, ordered by time" (common in audit views)
resource "postgresql_index" "audit_logs_resource_activity" {
  provider = postgresql.neon
  name     = "idx_audit_logs_resource_activity"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["resource_type", "resource_id", "created_at"]
  # Note: DESC ordering handled at query level for better performance
}

# For security monitoring: "all failed logins" or "all failed updates in last 24h"
resource "postgresql_index" "audit_logs_security_monitoring" {
  provider = postgresql.neon
  name     = "idx_audit_logs_security_monitoring"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["action_type", "outcome", "created_at"]
}