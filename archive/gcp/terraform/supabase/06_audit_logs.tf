# Audit Logs Table
# This file creates the audit_logs table and related ENUMs using null_resource

resource "null_resource" "create_audit_logs_table" {
  depends_on = [null_resource.create_database_schema]
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.supabase_password}" psql \
        -h "${var.supabase_host}" \
        -p "${var.supabase_port}" \
        -U "${var.supabase_username}" \
        -d "${var.supabase_database}" \
        -c "
      -- Create ENUM types for audit logs
      DO \$\$ BEGIN
          CREATE TYPE audit_action_enum AS ENUM ('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE audit_outcome_enum AS ENUM ('SUCCESS', 'FAILURE');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      DO \$\$ BEGIN
          CREATE TYPE resource_type_enum AS ENUM ('USER_PROFILE', 'PATIENT_PROFILE', 'PROVIDER_PROFILE', 'APPOINTMENT', 'MEDICAL_RECORD');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create audit_logs table
      CREATE TABLE IF NOT EXISTS audit_logs (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          user_id UUID NOT NULL REFERENCES user_profiles(id) ON DELETE CASCADE,
          action_type audit_action_enum NOT NULL,
          resource_type resource_type_enum NOT NULL,
          resource_id UUID,
          outcome audit_outcome_enum NOT NULL,
          old_values JSONB,
          new_values JSONB,
          details JSONB,
          source_ip INET,
          user_agent TEXT,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
      );

      -- Create indexes for audit_logs
      CREATE INDEX IF NOT EXISTS idx_audit_logs_user_action ON audit_logs(user_id, action_type);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_type ON audit_logs(resource_type, resource_id, action_type);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_outcome ON audit_logs(outcome);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
      "
    EOT
  }
  triggers = {
    schema_version = "2.0"
  }
}
