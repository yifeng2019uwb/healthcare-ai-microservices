#!/bin/bash
# =============================================================================
# run-migrations.sh — Apply schema migrations to Cloud SQL
# Usage:
#   ./run-migrations.sh              # run all pending migrations
#   ./run-migrations.sh V001         # run a specific migration by prefix
#
# Migrations are in schema/migrations/ and named V###__description.sql.
# Applied migrations are tracked in the schema_migrations table.
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATIONS_DIR="$SCRIPT_DIR/migrations"

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

"$PROXY_BIN" "$CLOUD_SQL_INSTANCE" --port=$DB_PORT &
PROXY_PID=$!
sleep 3

PGPASSWORD="$DB_PASSWORD" psql \
  -h 127.0.0.1 -p $DB_PORT \
  -U "$DB_USER" -d "$DB_NAME" \
  -c "SELECT 1" >/dev/null 2>&1 \
  || fail "Cloud SQL proxy failed to start"
ok "Proxy running (PID $PROXY_PID)"

trap 'kill $PROXY_PID 2>/dev/null; ok "Proxy stopped"' EXIT

# =============================================================================
# Bootstrap migration tracking table
# =============================================================================
stage "Bootstrapping migration tracking"

PGPASSWORD="$DB_PASSWORD" psql \
  -h 127.0.0.1 -p $DB_PORT \
  -U "$DB_USER" -d "$DB_NAME" \
  -c "CREATE TABLE IF NOT EXISTS schema_migrations (
        version     VARCHAR(10) PRIMARY KEY,
        description VARCHAR(255),
        applied_at  TIMESTAMPTZ DEFAULT NOW()
      );" >/dev/null
ok "schema_migrations table ready"

# =============================================================================
# Run migrations
# =============================================================================
FILTER="${1:-}"   # optional prefix filter, e.g. "V001"

stage "Applying migrations"

shopt -s nullglob
MIGRATION_FILES=("$MIGRATIONS_DIR"/V*.sql)
[[ ${#MIGRATION_FILES[@]} -eq 0 ]] && { warn "No migration files found in $MIGRATIONS_DIR"; exit 0; }

for file in "${MIGRATION_FILES[@]}"; do
  filename=$(basename "$file")
  version=$(echo "$filename" | grep -oE '^V[0-9]+')

  # If a filter was given, skip non-matching files
  [[ -n "$FILTER" && "$version" != "$FILTER" ]] && continue

  # Check if already applied
  applied=$(PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" \
    -tAc "SELECT COUNT(*) FROM schema_migrations WHERE version = '$version'")

  if [[ "$applied" -gt 0 ]]; then
    warn "$filename — already applied, skipping"
    continue
  fi

  warn "Applying $filename..."
  PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" \
    -f "$file"

  description=$(echo "$filename" | sed 's/^V[0-9]*__//; s/\.sql$//; s/_/ /g')
  PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" \
    -c "INSERT INTO schema_migrations(version, description) VALUES ('$version', '$description');" >/dev/null

  ok "$filename applied"
done

echo -e "\n${GREEN}=== Migrations complete ===${NC}\n"
