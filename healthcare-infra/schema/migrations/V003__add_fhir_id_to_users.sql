-- V003__add_fhir_id_to_users.sql
-- Adds fhir_id to users table: the UUID of the patient or provider record linked at registration.
-- Null for ADMIN users (no clinical profile).
-- Used to populate the fhirId JWT claim so downstream services can validate FHIR path params
-- without an extra DB lookup.

DO $$ BEGIN
    IF EXISTS (SELECT FROM pg_tables WHERE schemaname = 'public' AND tablename = 'users') THEN
        ALTER TABLE users ADD COLUMN IF NOT EXISTS fhir_id UUID;
        CREATE INDEX IF NOT EXISTS idx_users_fhir_id ON users(fhir_id);
    END IF;
END $$;
