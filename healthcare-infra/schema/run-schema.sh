#!/bin/bash
# =============================================================================
# run-schema.sh — Deploy database schema
#
# Usage:
#   ./run-schema.sh              # deploy all tables
#   ./run-schema.sh patients     # deploy single table
#
# Requires:
#   DATABASE_URL=postgresql://user:password@host:5432/dbname
#
# Example (Supabase):
#   DATABASE_URL="postgresql://postgres:<password>@db.<ref>.supabase.co:5432/postgres" \
#     ./run-schema.sh
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA_DIR="$SCRIPT_DIR/sql"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
warn()  { echo -e "${YELLOW}⚠ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

# Load .env if present and DATABASE_URL not already set
if [[ -z "$DATABASE_URL" ]] && [[ -f "$SCRIPT_DIR/../.env" ]]; then
  source "$SCRIPT_DIR/../.env"
  ok "Loaded .env"
fi

[[ -z "$DATABASE_URL" ]] && fail "DATABASE_URL is not set. Export it or add it to .env"

# Add PostgreSQL client tools to PATH (Mac Homebrew)
if [[ "$OSTYPE" == "darwin"* ]]; then
  export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"
fi

stage "Pre-flight: verifying connection"
psql "$DATABASE_URL" -c "SELECT 1" >/dev/null 2>&1 || fail "Cannot connect to database. Check DATABASE_URL."
ok "Connection verified"

# =============================================================================
# Table order — dependencies must come before dependents
# =============================================================================
ALL_TABLES=(
  "users"
  "organizations"
  "patients"
  "providers"
  "encounters"
  "conditions"
  "allergies"
  "audit_logs"
  "ai_analysis_results"
  "permissions"
  "triggers"
)

run_sql() {
  local table=$1
  local file="$SCHEMA_DIR/${table}.sql"
  [[ -f "$file" ]] || { warn "$file not found — skipped"; return; }
  psql "$DATABASE_URL" -f "$file"
  ok "Deployed $table"
}

TABLE=$1

if [[ -n "$TABLE" ]]; then
  stage "Deploying $TABLE"
  run_sql "$TABLE"
else
  stage "Deploying all tables"
  for table in "${ALL_TABLES[@]}"; do
    run_sql "$table"
  done
fi

echo -e "\n${GREEN}=== Schema deployment complete ===${NC}\n"
