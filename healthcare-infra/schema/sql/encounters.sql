-- encounters.sql
-- Source: Synthea encounters.csv
-- organization_id kept as snapshot — provider/org may change after encounter
-- payer_id stored as VARCHAR (no FK) — payers table is Phase 2
-- payer_coverage kept for reference — billing/insurance is Phase 2

CREATE TABLE IF NOT EXISTS encounters (
    id              UUID PRIMARY KEY,
    patient_id      UUID NOT NULL REFERENCES patients(id),
    provider_id     UUID NOT NULL REFERENCES providers(id),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    payer_id        VARCHAR(36),         -- Phase 2: add FK when payers table exists
    start_time      TIMESTAMPTZ NOT NULL,
    stop_time       TIMESTAMPTZ,
    encounter_class VARCHAR(50),
    code            VARCHAR(20),
    description     VARCHAR(255),
    base_cost       DECIMAL(10,2),
    total_cost      DECIMAL(10,2),
    payer_coverage  DECIMAL(10,2),       -- Phase 2: related to payer billing
    reason_code     VARCHAR(20),
    reason_desc     VARCHAR(255),
    created_at      TIMESTAMPTZ DEFAULT NOW()
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_by      VARCHAR(100) DEFAULT 'system'
);

CREATE INDEX IF NOT EXISTS idx_encounters_patient ON encounters(patient_id);
CREATE INDEX IF NOT EXISTS idx_encounters_provider ON encounters(provider_id);
CREATE INDEX IF NOT EXISTS idx_encounters_start_time ON encounters(start_time DESC);