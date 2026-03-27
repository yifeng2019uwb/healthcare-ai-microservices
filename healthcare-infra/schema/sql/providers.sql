-- providers.sql
-- Source: Synthea providers.csv
-- Requirements:
--   - Address removed — derived from organization_id JOIN (no redundancy)
--   - auth_id (UUID) links to users.id — null until provider registers
--   - provider_code — system generated, unique, required
--     Used for provider account registration — provider_code + first_name + last_name validation
--
-- Provider registration flow:
--   1. Admin creates provider record → provider_code auto-generated
--   2. Admin gives provider_code to provider
--   3. Provider registers account with provider_code + first_name + last_name

-- Sequence for provider_code auto-generation
CREATE SEQUENCE IF NOT EXISTS provider_code_seq START 1;

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
    provider_code   VARCHAR(20) UNIQUE NOT NULL
                    DEFAULT 'PRV-' || LPAD(nextval('provider_code_seq')::TEXT, 6, '0'),

    -- Contact and professional info
    phone           VARCHAR(20),         -- contact number
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
CREATE INDEX IF NOT EXISTS idx_providers_code ON providers(provider_code);
CREATE INDEX IF NOT EXISTS idx_providers_active ON providers(is_active);