-- Test database schema for PostgreSQL
-- This ensures enum handling is consistent between test and production

-- Create custom enums (same as production)
CREATE TYPE IF NOT EXISTS status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');
CREATE TYPE IF NOT EXISTS gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN');
CREATE TYPE IF NOT EXISTS role_enum AS ENUM ('PATIENT', 'PROVIDER');
CREATE TYPE IF NOT EXISTS appointment_status_enum AS ENUM ('AVAILABLE', 'SCHEDULED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW');
CREATE TYPE IF NOT EXISTS appointment_type_enum AS ENUM ('REGULAR_CONSULTATION', 'FOLLOW_UP', 'NEW_PATIENT_INTAKE', 'PROCEDURE_CONSULTATION');
CREATE TYPE IF NOT EXISTS record_type_enum AS ENUM ('DIAGNOSIS', 'TREATMENT', 'SUMMARY', 'LAB_RESULT', 'PRESCRIPTION', 'NOTE', 'OTHER');
CREATE TYPE IF NOT EXISTS action_type_enum AS ENUM ('CREATE', 'READ', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT');
CREATE TYPE IF NOT EXISTS resource_type_enum AS ENUM ('USER_PROFILE', 'PATIENT_PROFILE', 'PROVIDER_PROFILE', 'APPOINTMENT', 'MEDICAL_RECORD');
CREATE TYPE IF NOT EXISTS outcome_enum AS ENUM ('SUCCESS', 'FAILURE');

-- Create tables (same structure as production)
CREATE TABLE IF NOT EXISTS user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_auth_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender gender_enum,
    role role_enum NOT NULL,
    street_address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    status status_enum NOT NULL DEFAULT 'ACTIVE',
    custom_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS patient_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_profiles(id),
    patient_number VARCHAR(50) UNIQUE NOT NULL,
    medical_history JSONB,
    allergies JSONB,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(50),
    insurance_provider VARCHAR(100),
    insurance_policy_number VARCHAR(50),
    custom_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS provider_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_profiles(id),
    npi_number VARCHAR(10) UNIQUE NOT NULL,
    specialties TEXT[],
    qualifications TEXT,
    bio TEXT,
    office_address VARCHAR(255),
    office_phone VARCHAR(20),
    custom_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID REFERENCES patient_profiles(id),
    provider_id UUID NOT NULL REFERENCES provider_profiles(id),
    scheduled_at TIMESTAMPTZ NOT NULL,
    checkin_time TIMESTAMPTZ,
    appointment_type appointment_type_enum NOT NULL,
    status appointment_status_enum NOT NULL DEFAULT 'AVAILABLE',
    notes TEXT,
    custom_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS medical_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL REFERENCES appointments(id),
    record_type record_type_enum NOT NULL,
    content TEXT NOT NULL,
    is_patient_visible BOOLEAN NOT NULL DEFAULT FALSE,
    release_date TIMESTAMPTZ,
    custom_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action action_type_enum NOT NULL,
    resource_type resource_type_enum NOT NULL,
    resource_id UUID NOT NULL,
    user_id UUID,
    outcome outcome_enum NOT NULL,
    details JSONB,
    source_ip INET,
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes (same as production)
CREATE INDEX IF NOT EXISTS idx_user_profiles_external_auth_id ON user_profiles(external_auth_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_email ON user_profiles(email);
CREATE INDEX IF NOT EXISTS idx_user_profiles_status ON user_profiles(status);
CREATE INDEX IF NOT EXISTS idx_patient_profiles_user_id ON patient_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_patient_profiles_patient_number ON patient_profiles(patient_number);
CREATE INDEX IF NOT EXISTS idx_provider_profiles_user_id ON provider_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_provider_profiles_npi_number ON provider_profiles(npi_number);
CREATE INDEX IF NOT EXISTS idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_provider_id ON appointments(provider_id);
CREATE INDEX IF NOT EXISTS idx_appointments_scheduled_at ON appointments(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_medical_records_appointment_id ON medical_records(appointment_id);
CREATE INDEX IF NOT EXISTS idx_medical_records_record_type ON medical_records(record_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_resource_type ON audit_logs(resource_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
