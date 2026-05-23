-- providers.sql
-- Source: Synthea providers.csv
-- Requirements:
--   - Address removed — derived from organization_id JOIN (no redundancy)
--   - auth_id (UUID) links to users.id — null until provider registers

CREATE TABLE IF NOT EXISTS providers (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    gender          VARCHAR(1),
    speciality      VARCHAR(100),        -- Synthea spelling

    -- Synthea metrics
    encounters      INTEGER,
    procedures      INTEGER,

    -- Application fields
    auth_id         UUID UNIQUE,         -- links to users.id, null until registered

    -- Contact and professional info
    npi             VARCHAR(10) UNIQUE,  -- National Provider Identifier (10-digit federal CMS id)
    phone           VARCHAR(50),         -- contact number
    license_number  VARCHAR(50),         -- medical license
    is_active       BOOLEAN NOT NULL DEFAULT true,  -- active/inactive in system
    bio             TEXT,                -- provider bio/description

    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_by      VARCHAR(100) DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_providers_organization ON providers(organization_id);
CREATE INDEX IF NOT EXISTS idx_providers_auth_id ON providers(auth_id);
CREATE INDEX IF NOT EXISTS idx_providers_speciality ON providers(speciality);
CREATE INDEX IF NOT EXISTS idx_providers_active ON providers(is_active);
-- Registration lookup: match provider by name + org (Synthea data only; NPI/license added during onboarding)
CREATE INDEX IF NOT EXISTS idx_providers_name ON providers(name, organization_id);