-- V002__create_missing_tables.sql
-- Creates tables that were missing due to SQL syntax errors in the original DDL.
-- All statements use IF NOT EXISTS for idempotency.

-- 1. organizations (no FK dependencies)
CREATE TABLE IF NOT EXISTS organizations (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(50)  NOT NULL,
    zip         VARCHAR(20)  NOT NULL,
    phone       VARCHAR(50)  NOT NULL,
    lat         DECIMAL(10,6),
    lon         DECIMAL(10,6),
    revenue     DECIMAL(12,2),
    utilization INTEGER,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_by  VARCHAR(100) DEFAULT 'system'
);
CREATE INDEX IF NOT EXISTS idx_organizations_name ON organizations(name);

-- 2. providers (FK to organizations; includes npi so V001 ALTER TABLE becomes a no-op)
CREATE TABLE IF NOT EXISTS providers (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    gender          VARCHAR(1),
    speciality      VARCHAR(100),
    encounters      INTEGER,
    procedures      INTEGER,
    auth_id         UUID UNIQUE,
    provider_code   VARCHAR(20) UNIQUE NOT NULL
                    DEFAULT 'PRV-' || LPAD(nextval('provider_code_seq')::TEXT, 6, '0'),
    npi             VARCHAR(10) UNIQUE,
    phone           VARCHAR(20),
    license_number  VARCHAR(50),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    bio             TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_by      VARCHAR(100) DEFAULT 'system'
);
CREATE INDEX IF NOT EXISTS idx_providers_organization ON providers(organization_id);
CREATE INDEX IF NOT EXISTS idx_providers_auth_id      ON providers(auth_id);
CREATE INDEX IF NOT EXISTS idx_providers_speciality   ON providers(speciality);
CREATE INDEX IF NOT EXISTS idx_providers_code         ON providers(provider_code);
CREATE INDEX IF NOT EXISTS idx_providers_active       ON providers(is_active);
CREATE INDEX IF NOT EXISTS idx_providers_npi          ON providers(npi);

-- 3. encounters (FK to patients, providers, organizations)
CREATE TABLE IF NOT EXISTS encounters (
    id                  UUID PRIMARY KEY,
    patient_id          UUID NOT NULL REFERENCES patients(id),
    provider_id         UUID NOT NULL REFERENCES providers(id),
    organization_id     UUID NOT NULL REFERENCES organizations(id),
    payer_id            VARCHAR(36),
    start_time          TIMESTAMPTZ NOT NULL,
    stop_time           TIMESTAMPTZ,
    encounter_class     VARCHAR(50),
    code                VARCHAR(20),
    encounter_type      VARCHAR(50),
    status              VARCHAR(20),
    description         VARCHAR(255),
    base_cost           DECIMAL(10,2),
    total_cost          DECIMAL(10,2),
    payer_coverage      DECIMAL(10,2),
    reason_code         VARCHAR(20),
    reason_desc         VARCHAR(255),
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_by          VARCHAR(100) DEFAULT 'system'
);
CREATE INDEX IF NOT EXISTS idx_encounters_patient    ON encounters(patient_id);
CREATE INDEX IF NOT EXISTS idx_encounters_provider   ON encounters(provider_id);
CREATE INDEX IF NOT EXISTS idx_encounters_start_time ON encounters(start_time DESC);

-- 4. conditions (FK to patients, encounters; composite PK)
CREATE TABLE IF NOT EXISTS conditions (
    patient_id      UUID NOT NULL REFERENCES patients(id),
    encounter_id    UUID NOT NULL REFERENCES encounters(id),
    start_date      DATE NOT NULL,
    stop_date       DATE,
    system          VARCHAR(20) NOT NULL DEFAULT 'SNOMED-CT',
    code            VARCHAR(20) NOT NULL,
    description     VARCHAR(255) NOT NULL,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_by      VARCHAR(100) DEFAULT 'system',
    PRIMARY KEY (patient_id, encounter_id, code)
);
CREATE INDEX IF NOT EXISTS idx_conditions_patient ON conditions(patient_id);
CREATE INDEX IF NOT EXISTS idx_conditions_code    ON conditions(code);

-- 5. allergies (FK to patients, encounters; composite PK)
CREATE TABLE IF NOT EXISTS allergies (
    patient_id      UUID NOT NULL REFERENCES patients(id),
    encounter_id    UUID NOT NULL REFERENCES encounters(id),
    start_date      DATE NOT NULL,
    stop_date       DATE,
    code            VARCHAR(20) NOT NULL,
    system          VARCHAR(20),
    description     VARCHAR(255) NOT NULL,
    allergy_type    VARCHAR(20),
    category        VARCHAR(20),
    reaction1       VARCHAR(20),
    description1    VARCHAR(255),
    severity1       VARCHAR(10),
    reaction2       VARCHAR(20),
    description2    VARCHAR(255),
    severity2       VARCHAR(10),
    notes           TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_by      VARCHAR(100) DEFAULT 'system',
    PRIMARY KEY (patient_id, encounter_id, code)
);
CREATE INDEX IF NOT EXISTS idx_allergies_patient  ON allergies(patient_id);
CREATE INDEX IF NOT EXISTS idx_allergies_category ON allergies(category);
