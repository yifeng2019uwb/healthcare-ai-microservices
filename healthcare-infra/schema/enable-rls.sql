-- Enable Row Level Security on all tables.
-- Blocks Supabase public REST API (anon role) from reading/writing data.
-- Services connecting via the postgres superuser role bypass RLS automatically.
-- Safe to run multiple times (idempotent).

ALTER TABLE users                ENABLE ROW LEVEL SECURITY;
ALTER TABLE patients             ENABLE ROW LEVEL SECURITY;
ALTER TABLE providers            ENABLE ROW LEVEL SECURITY;
ALTER TABLE encounters           ENABLE ROW LEVEL SECURITY;
ALTER TABLE conditions           ENABLE ROW LEVEL SECURITY;
ALTER TABLE allergies            ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_analysis_results  ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs           ENABLE ROW LEVEL SECURITY;
ALTER TABLE organizations        ENABLE ROW LEVEL SECURITY;
