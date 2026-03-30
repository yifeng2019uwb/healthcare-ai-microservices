-- audit_logs.sql
-- HIPAA Security Rule 45 CFR § 164.312(b)
-- Records all access to ePHI (electronic Protected Health Information)
-- Retention: minimum 6 years per HIPAA requirement
-- Cannot be deleted — append only
-- Stored separately in Cloud Logging as backup for tamper-proof requirement

CREATE TABLE IF NOT EXISTS audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    -- WHO
    auth_id         VARCHAR(128),           -- user identity, null for system actions
    user_role       VARCHAR(20),            -- PATIENT, PROVIDER — minimum necessary rule
    -- WHAT
    action          VARCHAR(10) NOT NULL,   -- READ, CREATE, UPDATE, LOGIN, LOGOUT, DELETE
    resource_type   VARCHAR(50) NOT NULL,   -- patients, encounters, conditions, allergies
    resource_id     UUID,                   -- specific PHI record accessed
    -- OUTCOME
    outcome         VARCHAR(10) NOT NULL,   -- SUCCESS, FAILURE
    -- WHERE FROM
    source_ip       INET,                   -- request origin
    user_agent      TEXT,                   -- device/browser info
    -- WHEN
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for common audit queries
CREATE INDEX IF NOT EXISTS idx_audit_auth_id ON audit_logs(auth_id);
CREATE INDEX IF NOT EXISTS idx_audit_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at ON audit_logs(created_at DESC);