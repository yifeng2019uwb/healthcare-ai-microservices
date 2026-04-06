-- V001__add_npi_to_providers.sql
-- Adds NPI (National Provider Identifier) column to providers table.
-- NPI is a 10-digit federal CMS identifier (NPPES registry).
-- Kept alongside license_number — they serve different purposes:
--   license_number = state medical license
--   npi            = federal CMS identity (required for Medicare/Medicaid billing)

ALTER TABLE providers
    ADD COLUMN IF NOT EXISTS npi VARCHAR(10) UNIQUE;

CREATE INDEX IF NOT EXISTS idx_providers_npi ON providers(npi);
