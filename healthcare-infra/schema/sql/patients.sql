-- patients.sql
-- Source: Synthea patients.csv
-- Requirements:
--   - Store Synthea synthetic patient data as-is
--   - auth_id (UUID) links to users.id — null until user registers
--   - mrn (Medical Record Number) — system generated, unique, required
--     Used for patient account registration — MRN + first_name + last_name validation
--
-- Patient registration flow:
--   1. Provider creates patient record → MRN auto-generated
--   2. Provider gives MRN to patient
--   3. Patient registers account with MRN + first_name + last_name

-- Sequence for MRN auto-generation
CREATE SEQUENCE IF NOT EXISTS mrn_seq START 1;

CREATE TABLE IF NOT EXISTS patients (
    -- Synthea fields
    id                   UUID PRIMARY KEY,
    birthdate            DATE NOT NULL,
    deathdate            DATE,
    ssn                  VARCHAR(20),
    drivers              VARCHAR(20),
    passport             VARCHAR(20),
    prefix               VARCHAR(10),
    first_name           VARCHAR(100) NOT NULL,
    middle_name          VARCHAR(100),
    last_name            VARCHAR(100) NOT NULL,
    suffix               VARCHAR(10),
    maiden               VARCHAR(100),
    marital              VARCHAR(1),
    race                 VARCHAR(50),
    ethnicity            VARCHAR(50),
    gender               VARCHAR(10) NOT NULL,
    birthplace           VARCHAR(255),
    address              VARCHAR(255),
    city                 VARCHAR(100),
    state                VARCHAR(50),
    county               VARCHAR(100),
    fips                 VARCHAR(20),
    zip                  VARCHAR(10),
    lat                  DECIMAL(10,6),
    lon                  DECIMAL(10,6),
    healthcare_expenses  DECIMAL(12,2),
    healthcare_coverage  DECIMAL(12,2),
    income               INTEGER,

    -- Application fields
    auth_id              UUID UNIQUE,              -- links to users.id, null until registered
    mrn                  VARCHAR(20) UNIQUE NOT NULL
                         DEFAULT 'MRN-' || LPAD(nextval('mrn_seq')::TEXT, 6, '0'),

    -- Contact and medical info
    phone                VARCHAR(20),              -- contact number for appointments
    emergency_contact    VARCHAR(255),             -- emergency contact info
    blood_type           VARCHAR(5),               -- A+, B-, O+, etc. nullable
    notes                TEXT,                     -- general notes

    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_patients_auth_id ON patients(auth_id);
CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(last_name, first_name);
CREATE INDEX IF NOT EXISTS idx_patients_mrn ON patients(mrn);