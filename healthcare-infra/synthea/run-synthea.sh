#!/bin/bash
# =============================================================================
# run-synthea.sh — Generate Synthea data and load into Cloud SQL
# Usage:
#   ./run-synthea.sh              # generate (if needed) + load all
#   ./run-synthea.sh generate     # generate only
#   ./run-synthea.sh load         # load only (skip generation)
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SYNTHEA_DIR="$SCRIPT_DIR"
OUTPUT_DIR="$SYNTHEA_DIR/output/csv"
SYNTHEA_JAR="$SYNTHEA_DIR/synthea-with-dependencies.jar"
PROXY_BIN="$SCRIPT_DIR/../schema/cloud-sql-proxy"
DB_PORT=5432

# Colors
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()   { echo -e "${GREEN}✓ $1${NC}"; }
warn() { echo -e "${YELLOW}⚠ $1${NC}"; }
fail() { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage(){ echo -e "\n${BLUE}=== $1 ===${NC}"; }

# Add PostgreSQL client tools to PATH (Mac Homebrew)
if [[ "$OSTYPE" == "darwin"* ]]; then
  export PATH="/opt/homebrew/opt/postgresql@15/bin:$PATH"
fi

# Load .env if local
if [[ -z "$CI" ]] && [[ -f "$SCRIPT_DIR/../.env" ]]; then
  source "$SCRIPT_DIR/../.env"
  ok "Loaded .env"
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

# Read DB password from Secret Manager
stage "Reading credentials"
DB_PASSWORD=$(gcloud secrets versions access latest \
  --secret=db-password \
  --project="$GCP_PROJECT_ID") \
  || fail "Failed to read db-password from Secret Manager"
ok "Credentials loaded"

COMMAND=${1:-all}

# =============================================================================
# psql helper — runs SQL piped via stdin (allows variable expansion)
# =============================================================================
ERRORS=()

run_psql() {
  local label=$1
  local sql=$2
  echo "$sql" | PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" \
    && ok "Loaded $label" \
    || { warn "Failed: $label"; ERRORS+=("$label"); }
}

# =============================================================================
# Generate Synthea data
# =============================================================================
generate() {
  stage "Synthea data generation"

  if [[ -f "$OUTPUT_DIR/patients.csv" ]]; then
    warn "CSV files already exist — skipping generation"
    warn "Delete $OUTPUT_DIR to regenerate"
    return
  fi

  [[ -f "$SYNTHEA_JAR" ]] || fail "Synthea JAR not found at $SYNTHEA_JAR"

  java -jar "$SYNTHEA_JAR" \
    -p 200 \
    --exporter.csv.export=true \
    --exporter.fhir.export=false \
    --exporter.baseDirectory="$SYNTHEA_DIR/output" \
    Washington Seattle

  ok "Generated 200 patients"
}

# =============================================================================
# Start Cloud SQL Auth Proxy
# =============================================================================
start_proxy() {
  stage "Starting Cloud SQL Auth Proxy"

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

  echo "SELECT 1" | PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1 \
    || fail "Cloud SQL proxy failed to start"

  ok "Proxy running (PID $PROXY_PID)"
  trap 'kill $PROXY_PID 2>/dev/null; ok "Proxy stopped"' EXIT
}

# =============================================================================
# Load data
# All tables use: COPY into temp table → INSERT ON CONFLICT DO NOTHING
# This makes load idempotent — safe to run multiple times
# =============================================================================

# Helper: pipe SQL to psql, track failures
load_table() {
  local label=$1
  local sql=$2
  echo "$sql" | PGPASSWORD="$DB_PASSWORD" psql \
    -h 127.0.0.1 -p $DB_PORT \
    -U "$DB_USER" -d "$DB_NAME" \
    && ok "Loaded $label" \
    || { warn "Failed: $label"; ERRORS+=("$label"); }
}

load() {
  stage "Loading Synthea data"

  # Setup sequences and defaults — idempotent, safe to run every time
  load_table "sequences" "
    CREATE SEQUENCE IF NOT EXISTS mrn_seq START 1;
    ALTER TABLE patients ALTER COLUMN mrn SET DEFAULT 'MRN-' || LPAD(nextval('mrn_seq')::TEXT, 6, '0');
    CREATE SEQUENCE IF NOT EXISTS provider_code_seq START 1;
    ALTER TABLE providers ALTER COLUMN provider_code SET DEFAULT 'PRV-' || LPAD(nextval('provider_code_seq')::TEXT, 6, '0');
  "

  # patients
  load_table "patients" "
    CREATE TEMP TABLE patients_import (
      id UUID, birthdate DATE, deathdate DATE, ssn VARCHAR(20),
      drivers VARCHAR(20), passport VARCHAR(20), prefix VARCHAR(10),
      first_name VARCHAR(100), middle_name VARCHAR(100), last_name VARCHAR(100),
      suffix VARCHAR(10), maiden VARCHAR(100), marital VARCHAR(1),
      race VARCHAR(50), ethnicity VARCHAR(50), gender VARCHAR(10),
      birthplace VARCHAR(255), address VARCHAR(255), city VARCHAR(100),
      state VARCHAR(50), county VARCHAR(100), fips VARCHAR(20), zip VARCHAR(10),
      lat DECIMAL(10,6), lon DECIMAL(10,6),
      healthcare_expenses DECIMAL(12,2), healthcare_coverage DECIMAL(12,2),
      income INTEGER
    );
    \COPY patients_import FROM '$OUTPUT_DIR/patients.csv' CSV HEADER;
    INSERT INTO patients (id,birthdate,deathdate,ssn,drivers,passport,prefix,first_name,middle_name,last_name,suffix,maiden,marital,race,ethnicity,gender,birthplace,address,city,state,county,fips,zip,lat,lon,healthcare_expenses,healthcare_coverage,income)
    SELECT id,birthdate,deathdate,ssn,drivers,passport,prefix,first_name,middle_name,last_name,suffix,maiden,marital,race,ethnicity,gender,birthplace,address,city,state,county,fips,zip,lat,lon,healthcare_expenses,healthcare_coverage,income
    FROM patients_import ON CONFLICT (id) DO NOTHING;
  "

  # organizations
  load_table "organizations" "
    CREATE TEMP TABLE orgs_import (
      id UUID, name VARCHAR(255), address VARCHAR(255), city VARCHAR(100),
      state VARCHAR(50), zip VARCHAR(20), lat DECIMAL(10,6), lon DECIMAL(10,6),
      phone VARCHAR(50), revenue DECIMAL(12,2), utilization INTEGER
    );
    \COPY orgs_import FROM '$OUTPUT_DIR/organizations.csv' CSV HEADER;
    INSERT INTO organizations (id,name,address,city,state,zip,lat,lon,phone,revenue,utilization)
    SELECT id,name,address,city,state,zip,lat,lon,phone,revenue,utilization
    FROM orgs_import ON CONFLICT (id) DO NOTHING;
  "

  # providers — CSV has address columns not in schema, use temp table to skip them
  load_table "providers" "
    CREATE TEMP TABLE providers_import (
      id UUID, organization_id UUID, name VARCHAR(255), gender VARCHAR(1),
      speciality VARCHAR(100), address VARCHAR(255), city VARCHAR(100),
      state VARCHAR(50), zip VARCHAR(20), lat DECIMAL(10,6), lon DECIMAL(10,6),
      encounters INTEGER, procedures INTEGER
    );
    \COPY providers_import FROM '$OUTPUT_DIR/providers.csv' CSV HEADER;
    INSERT INTO providers (id,organization_id,name,gender,speciality,encounters,procedures)
    SELECT id,organization_id,name,gender,speciality,encounters,procedures
    FROM providers_import ON CONFLICT (id) DO NOTHING;
  "

  # encounters
  load_table "encounters" "
    CREATE TEMP TABLE encounters_import (
      id UUID, start_time TIMESTAMPTZ, stop_time TIMESTAMPTZ,
      patient_id UUID, organization_id UUID, provider_id UUID,
      payer_id VARCHAR(36), encounter_class VARCHAR(50),
      code VARCHAR(20), description VARCHAR(255),
      base_cost DECIMAL(10,2), total_cost DECIMAL(10,2),
      payer_coverage DECIMAL(10,2), reason_code VARCHAR(20), reason_desc VARCHAR(255)
    );
    \COPY encounters_import FROM '$OUTPUT_DIR/encounters.csv' CSV HEADER;
    INSERT INTO encounters (id,start_time,stop_time,patient_id,organization_id,provider_id,payer_id,encounter_class,code,description,base_cost,total_cost,payer_coverage,reason_code,reason_desc)
    SELECT id,start_time,stop_time,patient_id,organization_id,provider_id,payer_id,encounter_class,code,description,base_cost,total_cost,payer_coverage,reason_code,reason_desc
    FROM encounters_import ON CONFLICT (id) DO NOTHING;
  "

  # conditions
  load_table "conditions" "
    CREATE TEMP TABLE conditions_import (
      start_date DATE, stop_date DATE, patient_id UUID, encounter_id UUID,
      system VARCHAR(20), code VARCHAR(20), description VARCHAR(255)
    );
    \COPY conditions_import FROM '$OUTPUT_DIR/conditions.csv' CSV HEADER;
    INSERT INTO conditions (start_date,stop_date,patient_id,encounter_id,system,code,description)
    SELECT start_date,stop_date,patient_id,encounter_id,system,code,description
    FROM conditions_import ON CONFLICT (patient_id,encounter_id,code) DO NOTHING;
  "

  # allergies
  load_table "allergies" "
    CREATE TEMP TABLE allergies_import (
      start_date DATE, stop_date DATE, patient_id UUID, encounter_id UUID,
      code VARCHAR(20), system VARCHAR(20), description VARCHAR(255),
      allergy_type VARCHAR(20), category VARCHAR(20),
      reaction1 VARCHAR(20), description1 VARCHAR(255), severity1 VARCHAR(10),
      reaction2 VARCHAR(20), description2 VARCHAR(255), severity2 VARCHAR(10)
    );
    \COPY allergies_import FROM '$OUTPUT_DIR/allergies.csv' CSV HEADER;
    INSERT INTO allergies (start_date,stop_date,patient_id,encounter_id,code,system,description,allergy_type,category,reaction1,description1,severity1,reaction2,description2,severity2)
    SELECT start_date,stop_date,patient_id,encounter_id,code,system,description,allergy_type,category,reaction1,description1,severity1,reaction2,description2,severity2
    FROM allergies_import ON CONFLICT (patient_id,encounter_id,code) DO NOTHING;
  "

  # Report failures
  if [[ ${#ERRORS[@]} -gt 0 ]]; then
    echo -e "\n${RED}=== Failed tables ===${NC}"
    for err in "${ERRORS[@]}"; do
      echo -e "${RED}✗ $err${NC}"
    done
    exit 1
  fi
}

# =============================================================================
# Commands
# =============================================================================
case $COMMAND in
  generate)
    generate
    ;;
  load)
    start_proxy
    load
    ;;
  all)
    generate
    start_proxy
    load
    ;;
  *)
    fail "Unknown command: $COMMAND — use generate, load, all"
    ;;
esac

echo -e "\n${GREEN}=== Done ===${NC}\n"