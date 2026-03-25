-- patients.sql
-- Source: Synthea patients.csv
-- Requirements:
--   - Store Synthea synthetic patient data as-is
--   - Add firebase_uid for auth linkage (nullable — Synthea data loads first)
--   - No real patient data — all Synthea generated, regeneratable
--   - Flexible VARCHAR lengths with room for extension

CREATE TABLE IF NOT EXISTS patients (
    -- Synthea fields
    id              UUID PRIMARY KEY,
    birthdate       DATE NOT NULL,
    deathdate       DATE,
    ssn             VARCHAR(20),
    drivers         VARCHAR(20),
    passport        VARCHAR(20),
    prefix          VARCHAR(10),
    first_name      VARCHAR(100) NOT NULL,
    middle_name     VARCHAR(100),
    last_name       VARCHAR(100) NOT NULL,
    suffix          VARCHAR(10),
    maiden          VARCHAR(100),
    marital         VARCHAR(1),
    race            VARCHAR(50),
    ethnicity       VARCHAR(50),
    gender          VARCHAR(10) NOT NULL,
    birthplace      VARCHAR(255),
    address         VARCHAR(255),
    city            VARCHAR(100),
    state           VARCHAR(50),
    county          VARCHAR(100),
    fips            VARCHAR(20),
    zip             VARCHAR(10),
    lat             DECIMAL(10,6),
    lon             DECIMAL(10,6),
    healthcare_expenses  DECIMAL(12,2),
    healthcare_coverage  DECIMAL(12,2),
    income          INTEGER,

    -- Application fields
    auth_id         VARCHAR(128) UNIQUE,        -- links to auth provider, null until user registers (Firebase UID)
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_patients_auth_id ON patients(auth_id);
CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(last_name, first_name);