-- allergies.sql
-- Source: Synthea allergies.csv
-- SYSTEM can be 'Unknown' — not always RxNorm/SNOMED-CT
-- REACTION1/2 supports two reactions max (Synthea limitation)
-- notes field added for additional reactions or clinical observations

CREATE TABLE IF NOT EXISTS allergies (
    patient_id      UUID NOT NULL REFERENCES patients(id),
    encounter_id    UUID NOT NULL REFERENCES encounters(id),
    start_date      DATE NOT NULL,
    stop_date       DATE,
    code            VARCHAR(20) NOT NULL,
    system          VARCHAR(20),
    description     VARCHAR(255) NOT NULL,
    allergy_type    VARCHAR(20),             -- allergy or intolerance
    category        VARCHAR(20),             -- environment, food, drug
    reaction1       VARCHAR(20),
    description1    VARCHAR(255),
    severity1       VARCHAR(10),             -- MILD, MODERATE, SEVERE
    reaction2       VARCHAR(20),
    description2    VARCHAR(255),
    severity2       VARCHAR(10),
    notes           TEXT,                    -- additional reactions or clinical observations

    PRIMARY KEY (patient_id, encounter_id, code)
);

CREATE INDEX IF NOT EXISTS idx_allergies_patient ON allergies(patient_id);
CREATE INDEX IF NOT EXISTS idx_allergies_category ON allergies(category);