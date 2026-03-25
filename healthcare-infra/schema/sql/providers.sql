-- providers.sql
-- Source: Synthea providers.csv
-- Address removed — derived from organization_id JOIN
-- Synthea duplicates address in CSV (flat file limitation) — not needed in relational DB

CREATE TABLE IF NOT EXISTS providers (
    id              UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations(id),
    name            VARCHAR(255) NOT NULL,
    gender          VARCHAR(1),
    speciality      VARCHAR(100),        -- Synthea spelling
    encounters      INTEGER,             -- Synthea metric, nullable
    procedures      INTEGER,             -- Synthea metric, nullable
    auth_id         VARCHAR(128) UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_providers_organization ON providers(organization_id);
CREATE INDEX IF NOT EXISTS idx_providers_auth_id ON providers(auth_id);
CREATE INDEX IF NOT EXISTS idx_providers_speciality ON providers(speciality);