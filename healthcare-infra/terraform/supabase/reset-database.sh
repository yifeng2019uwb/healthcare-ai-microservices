#!/bin/bash

# Reset Database Schema for Healthcare AI Microservices
# This script drops all existing tables and recreates them with the correct schema

echo "🔄 Resetting Supabase database schema..."

# Load variables from terraform.tfvars
source terraform.tfvars

# Drop all tables in correct order (reverse dependency order)
echo "🗑️  Dropping existing tables..."

PGPASSWORD="${supabase_password}" psql \
  -h "${supabase_host}" \
  -p "${supabase_port}" \
  -U "${supabase_username}" \
  -d "${supabase_database}" \
  -c "
-- Drop tables in reverse dependency order
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS medical_records CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS provider_profiles CASCADE;
DROP TABLE IF EXISTS patient_profiles CASCADE;
DROP TABLE IF EXISTS user_profiles CASCADE;

-- Drop ENUM types
DROP TYPE IF EXISTS audit_action_enum CASCADE;
DROP TYPE IF EXISTS audit_outcome_enum CASCADE;
DROP TYPE IF EXISTS resource_type_enum CASCADE;
DROP TYPE IF EXISTS medical_record_type_enum CASCADE;
DROP TYPE IF EXISTS appointment_status_enum CASCADE;
DROP TYPE IF EXISTS appointment_type_enum CASCADE;
DROP TYPE IF EXISTS gender_enum CASCADE;
DROP TYPE IF EXISTS role_enum CASCADE;
DROP TYPE IF EXISTS status_enum CASCADE;
"

echo "✅ Tables dropped successfully!"

# Now run the deployment script to recreate with correct schema
echo "🚀 Recreating tables with correct schema..."
./deploy-supabase.sh

echo "🎉 Database reset complete!"
