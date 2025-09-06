# Audit Logs Table
# This file creates the audit_logs table and related ENUMs using null_resource

resource "null_resource" "create_audit_logs_table" {
  provisioner "local-exec" {
    command = <<-EOT
      PGPASSWORD="${var.neon_password}" psql \
        -h "${var.neon_host}" \
        -p "${var.neon_port}" \
        -U "${var.neon_username}" \
        -d "${var.neon_database}" \
        -c "
      -- Create ENUM types for audit logs
      DO \$\$ BEGIN
          CREATE TYPE audit_action_enum AS ENUM ('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'EXPORT', 'IMPORT', 'BACKUP', 'RESTORE');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;
      
      DO \$\$ BEGIN
          CREATE TYPE audit_status_enum AS ENUM ('SUCCESS', 'FAILURE', 'PENDING', 'CANCELLED');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;
      
      DO \$\$ BEGIN
          CREATE TYPE resource_type_enum AS ENUM ('USER_PROFILE', 'PATIENT_PROFILE', 'PROVIDER_PROFILE', 'APPOINTMENT', 'MEDICAL_RECORD', 'AUDIT_LOG', 'SYSTEM');
      EXCEPTION
          WHEN duplicate_object THEN null;
      END \$\$;

      -- Create audit_logs table
      CREATE TABLE IF NOT EXISTS audit_logs (
          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
          user_id UUID REFERENCES user_profiles(id) ON DELETE SET NULL,
          action audit_action_enum NOT NULL,
          resource_type resource_type_enum NOT NULL,
          resource_id UUID,
          old_values JSONB,
          new_values JSONB,
          status audit_status_enum NOT NULL DEFAULT 'SUCCESS',
          error_message TEXT,
          ip_address INET,
          user_agent TEXT,
          session_id VARCHAR(255),
          request_id VARCHAR(255),
          duration_ms INTEGER,
          metadata JSONB,
          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
      );

      -- Create indexes for audit_logs
      CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_type ON audit_logs(resource_type);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_id ON audit_logs(resource_id);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_status ON audit_logs(status);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_ip_address ON audit_logs(ip_address);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_session_id ON audit_logs(session_id);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_request_id ON audit_logs(request_id);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_user_action ON audit_logs(user_id, action);
      CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_action ON audit_logs(resource_type, resource_id, action);
      "
    EOT
  }
  triggers = {
    schema_version = "1.0"
  }
}
