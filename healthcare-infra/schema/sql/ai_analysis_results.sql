-- ai_analysis_results.sql
-- Append-only AI governance table. Immutable after insert.
-- Each Gemini call produces one new row — full history is preserved.
-- Governance fields (trigger_type, triggered_by, model_version, input_record_ids)
-- are required for auditability and explainability.

CREATE TABLE IF NOT EXISTS ai_analysis_results (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID         NOT NULL REFERENCES patients(id),
    generated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    summary          TEXT         NOT NULL,
    risk_flags       JSONB        NOT NULL,           -- [{flag, reason}]
    -- AI governance fields (immutable after insert)
    trigger_type     VARCHAR(50)  NOT NULL,            -- CONDITION_ADDED, ALLERGY_ADDED, etc.
    triggered_by     UUID,                             -- provider user_id; null if debounce merged multiple providers
    model_version    VARCHAR(100) NOT NULL,            -- e.g. 'gemini-1.5-pro-002'
    input_record_ids JSONB        NOT NULL,            -- UUIDs of conditions/allergies included in this call
    archived         BOOLEAN      NOT NULL DEFAULT FALSE, -- set by nightly maintenance; rows are never hard-deleted
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
    -- no updated_at / updated_by: this table is append-only, rows never change after insert
    -- exception: archived flag is set by maintenance job (not a business update)
);

-- composite index: covers all patient history queries (patient_id lookup + time ordering)
CREATE INDEX IF NOT EXISTS idx_ai_results_patient_history
    ON ai_analysis_results (patient_id, generated_at DESC);

-- standalone index: time-range governance queries across all patients
CREATE INDEX IF NOT EXISTS idx_ai_results_generated_at
    ON ai_analysis_results (generated_at DESC);

-- TODO: idx_ai_results_model_version (model_version, generated_at DESC)
--       add when model drift monitoring is implemented
