-- permissions.sql
-- PostgreSQL roles and grants — one role per service
-- Run after all tables are created
-- Each service connects with its own DB user assigned a restricted role
-- Even if one service is compromised, attacker can only access
-- that service's permitted tables

-- =============================================================================
-- Create roles
-- =============================================================================
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'auth_role') THEN
        CREATE ROLE auth_role;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'patient_role') THEN
        CREATE ROLE patient_role;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'provider_role') THEN
        CREATE ROLE provider_role;
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'appointment_role') THEN
        CREATE ROLE appointment_role;
    END IF;
END $$;

-- =============================================================================
-- auth_role — users table only
-- No other service can read or write credentials
-- =============================================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON users TO auth_role;

-- =============================================================================
-- patient_role
-- Owns: patients, conditions, allergies
-- Reads: providers, organizations, encounters
-- =============================================================================
GRANT SELECT, INSERT, UPDATE ON patients TO patient_role;
GRANT SELECT, INSERT, UPDATE ON conditions TO patient_role;
GRANT SELECT, INSERT, UPDATE ON allergies TO patient_role;
GRANT SELECT ON providers TO patient_role;
GRANT SELECT ON organizations TO patient_role;
GRANT SELECT ON encounters TO patient_role;
GRANT INSERT ON audit_logs TO patient_role;

-- =============================================================================
-- provider_role
-- Owns: providers, organizations
-- Writes clinical data: conditions, allergies, encounters (after visit)
-- Reads: patients
-- =============================================================================
GRANT SELECT, INSERT, UPDATE ON providers TO provider_role;
GRANT SELECT, INSERT, UPDATE ON organizations TO provider_role;
GRANT SELECT, INSERT, UPDATE ON conditions TO provider_role;
GRANT SELECT, INSERT, UPDATE ON allergies TO provider_role;
GRANT SELECT, INSERT, UPDATE ON encounters TO provider_role;
GRANT SELECT ON patients TO provider_role;
GRANT INSERT ON audit_logs TO provider_role;

-- =============================================================================
-- appointment_role
-- Owns: encounters
-- Reads: patients, providers, organizations
-- =============================================================================
GRANT SELECT, INSERT, UPDATE ON encounters TO appointment_role;
GRANT SELECT ON patients TO appointment_role;
GRANT SELECT ON providers TO appointment_role;
GRANT SELECT ON organizations TO appointment_role;
GRANT INSERT ON audit_logs TO appointment_role;

-- =============================================================================
-- Create service users and assign roles
-- Passwords managed via GCP Secret Manager — set separately
-- =============================================================================
DO $$ BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'auth_service_user') THEN
        CREATE USER auth_service_user WITH PASSWORD 'PLACEHOLDER_SET_VIA_SECRET_MANAGER';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'patient_service_user') THEN
        CREATE USER patient_service_user WITH PASSWORD 'PLACEHOLDER_SET_VIA_SECRET_MANAGER';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'provider_service_user') THEN
        CREATE USER provider_service_user WITH PASSWORD 'PLACEHOLDER_SET_VIA_SECRET_MANAGER';
    END IF;
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'appointment_service_user') THEN
        CREATE USER appointment_service_user WITH PASSWORD 'PLACEHOLDER_SET_VIA_SECRET_MANAGER';
    END IF;
END $$;

GRANT auth_role TO auth_service_user;
GRANT patient_role TO patient_service_user;
GRANT provider_role TO provider_service_user;
GRANT appointment_role TO appointment_service_user;