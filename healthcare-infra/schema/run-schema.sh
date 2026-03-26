#!/bin/bash
# =============================================================================
# run-schema.sh — Deploy database schema to Cloud SQL
# Usage:
#   ./run-schema.sh              # deploy all tables
#   ./run-schema.sh patients     # deploy single table
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA_DIR="$SCRIPT_DIR/sql"

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage(){ echo -e "\n${BLUE}=== $1 ===${NC}"; }

# Load .env if local
if [[ -z "$CI" ]] && [[ -f "$SCRIPT_DIR/../.env" ]]; then
  source "$SCRIPT_DIR/../.env"
  ok "Loaded .env"
fi

# Add PostgreSQL client tools to PATH (Mac Homebrew)
if [[ "$OSTYPE" == "darwin"* ]]; then
  export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"
fi

# Add PostgreSQL client tools to PATH (Mac Homebrew)
if [[ "$OSTYPE" == "darwin"* ]]; then
  export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"
fi

# =============================================================================
# Validate required env vars
# =============================================================================
stage "Pre-flight checks"

required_vars=(
  "CLOUD_SQL_INSTANCE"
  "DB_NAME"
  "DB_USER"
  "GCP_PROJECT_ID"
)

for var in "${required_vars[@]}"; do
  [[ -z "${!var}" ]] && fail "$var is not set"
done
ok "All required env vars present"

# =============================================================================
# Read credentials from Secret Manager
# =============================================================================
stage "Reading credentials"
DB_PASSWORD=$(gcloud secrets versions access latest \
  --secret=db-password \
  --project="$GCP_PROJECT_ID") \
  || fail "Failed to read db-password from Secret Manager"
ok "Credentials loaded"

# =============================================================================
# Start Cloud SQL Auth Proxy
# =============================================================================
stage "Starting Cloud SQL Auth Proxy"

PROXY_BIN="$SCRIPT_DIR/cloud-sql-proxy"
DB_PORT=5432

# Download proxy if not present
if [[ ! -f "$PROXY_BIN" ]]; then
  warn "Downloading Cloud SQL Auth Proxy..."
  if [[ "$OSTYPE" == "darwin"* ]]; then
    PROXY_URL="https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.0/cloud-sql-proxy.darwin.amd64"
  else
    PROXY_URL="https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.0/cloud-sql-proxy.linux.amd64"
  fi
  curl -sLo "$PROXY_BIN" "$PROXY_URL"
  chmod +x "$PROXY_BIN"
  ok "Downloaded"
fi

# Start proxy in background
"$PROXY_BIN" "$CLOUD_SQL_INSTANCE" --port=$DB_PORT &
PROXY_PID=$!
sleep 3

# Verify proxy is up
PGPASSWORD="$DB_PASSWORD" psql \
  -h 127.0.0.1 -p $DB_PORT \
  -U "$DB_USER" -d "$DB_NAME" \
  -c "SELECT 1" >/dev/null 2>&1 \
  || fail "Cloud SQL proxy failed to start"
ok "Proxy running (PID $PROXY_PID)"

# Stop proxy on exit
trap 'kill $PROXY_PID 2>/dev/null; ok "Proxy stopped"' EXIT

# =============================================================================
# Deploy schema
# =============================================================================
TABLE=$1

# Ordered list — dependencies must come first
ALL_TABLES=(
  "users"
  "patients"
  "organizations"
  "providers"
  "encounters"
  "conditions"
  "allergies"
  "audit_logs"
  "permissions"
)

run_sql() {
  local table=$1
  local file="$SCHEMA_DIR/${table}.sql"

  [[ -f "$file" ]] || { warn "$file not found — skipped"; return; }

  PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" \
    -f "$file"
  ok "Deployed $table"
}

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