-- AI Analysis Tables
-- Run against the healthcare database after the base schema is in place.
-- Both tables reference patients(id) — ensure patients table exists first.

-- ---------------------------------------------------------------------------
-- ai_analysis_jobs
-- Outbox table: one row per patient, drives the @Scheduled AI consumer.
-- Written by ConditionService/AllergyService on clinical record saves (UPSERT).
-- Lifecycle: PENDING → PROCESSING → COMPLETED | FAILED
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ai_analysis_jobs (
    patient_id      UUID        PRIMARY KEY REFERENCES patients(id),
    marked_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    triggered_by    UUID        NULL,
    trigger_type    VARCHAR(50) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    lock_expires_at TIMESTAMPTZ NULL,
    completed_at    TIMESTAMPTZ NULL,
    last_error      TEXT        NULL,
    retry_count     INT         NOT NULL DEFAULT 0,
    next_retry_at   TIMESTAMPTZ NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_jobs_status_marked_at
    ON ai_analysis_jobs (status, marked_at);

-- ---------------------------------------------------------------------------
-- ai_analysis_results
-- Append-only governance table. Immutable after insert.
-- Each Gemini call produces one new row — history is preserved.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS ai_analysis_results (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID        NOT NULL REFERENCES patients(id),
    generated_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    summary          TEXT        NOT NULL,
    risk_flags       JSONB       NOT NULL,
    trigger_type     VARCHAR(50) NOT NULL,
    triggered_by     UUID        NULL,
    model_version    VARCHAR(100) NOT NULL,
    input_record_ids JSONB       NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_results_patient_id
    ON ai_analysis_results (patient_id);

CREATE INDEX IF NOT EXISTS idx_ai_results_generated_at
    ON ai_analysis_results (generated_at DESC);
