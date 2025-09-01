# Audit logs table - Track all data changes for compliance and security
resource "postgresql_table" "audit_logs" {
  provider = postgresql.neon
  name     = "audit_logs"
  schema   = postgresql_schema.public.name

  column {
    name     = "id"
    type     = "UUID"
    null_able = false
    default  = "gen_random_uuid()"
  }

  column {
    name     = "table_name"
    type     = "VARCHAR(100)"
    null_able = false
  }

  column {
    name     = "record_id"
    type     = "UUID"
    null_able = false
  }

  column {
    name     = "operation"
    type     = "VARCHAR(20)"
    null_able = false
  }

  column {
    name     = "user_id"
    type     = "UUID"
    null_able = true
  }

  column {
    name     = "user_type"
    type     = "VARCHAR(20)"
    null_able = true
  }

  column {
    name     = "old_values"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "new_values"
    type     = "JSONB"
    null_able = true
  }

  column {
    name     = "ip_address"
    type     = "INET"
    null_able = true
  }

  column {
    name     = "user_agent"
    type     = "TEXT"
    null_able = true
  }

  column {
    name     = "session_id"
    type     = "VARCHAR(255)"
    null_able = true
  }

  column {
    name     = "created_at"
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

# Create index on table_name for faster lookups
resource "postgresql_index" "audit_logs_table_name" {
  provider = postgresql.neon
  name     = "idx_audit_logs_table_name"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["table_name"]
}

# Create index on record_id for faster lookups
resource "postgresql_index" "audit_logs_record_id" {
  provider = postgresql.neon
  name     = "idx_audit_logs_record_id"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["record_id"]
}

# Create index on user_id for user activity tracking
resource "postgresql_index" "audit_logs_user_id" {
  provider = postgresql.neon
  name     = "idx_audit_logs_user_id"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["user_id"]
}

# Create index on operation for filtering by action type
resource "postgresql_index" "audit_logs_operation" {
  provider = postgresql.neon
  name     = "idx_audit_logs_operation"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["operation"]
}

# Create composite index for time-based queries
resource "postgresql_index" "audit_logs_created_at" {
  provider = postgresql.neon
  name     = "idx_audit_logs_created_at"
  table    = postgresql_table.audit_logs.name
  schema   = postgresql_schema.public.name
  columns  = ["created_at"]
}
