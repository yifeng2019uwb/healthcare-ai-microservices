-- conditions.sql
-- Source: Synthea conditions.csv
-- No single PK in Synthea — composite key (patient_id, encounter_id, code)
-- STOP nullable — empty means ongoing condition
-- SYSTEM always SNOMED-CT in Synthea but kept for future data sources

CREATE TABLE IF NOT EXISTS conditions (
    patient_id      UUID NOT NULL REFERENCES patients(id),
    encounter_id    UUID NOT NULL REFERENCES encounters(id),
    start_date      DATE NOT NULL,
    stop_date       DATE,
    system          VARCHAR(20) NOT NULL DEFAULT 'SNOMED-CT',
    code            VARCHAR(20) NOT NULL,
    description     VARCHAR(255) NOT NULL,

    PRIMARY KEY (patient_id, encounter_id, code)
);

CREATE INDEX IF NOT EXISTS idx_conditions_patient ON conditions(patient_id);
CREATE INDEX IF NOT EXISTS idx_conditions_code ON conditions(code);