#!/bin/bash
# =============================================================================
# run-it.sh — Run integration tests against the deployed gateway
# =============================================================================
# Usage:
#   ./integration_tests/run-it.sh <test|all> [test2 ...]
#
# Tests:
#   seed          — verify test accounts are reachable
#   auth          — auth.AuthIT  (JUnit 5 via Failsafe)
#   register      — auth.RegisterPatientIT + auth.RegisterProviderIT
#   patient       — patient.PatientProfileIT
#   provider      — provider.ProviderProfileIT
#   admin         — admin.AdminImportIT  (requires test-data: run-synthea.sh test-data <n>)
#   all           — run all tests in order
#
# Migration status:
#   auth/admin → JUnit 5 (Failsafe)
#   others     → legacy main() style (exec plugin), to be migrated
#
# Examples:
#   ./integration_tests/run-it.sh auth
#   ./integration_tests/run-it.sh patient provider
#   ./integration_tests/run-it.sh admin
#   ./integration_tests/run-it.sh all
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
GATEWAY_URL="${GATEWAY_URL:-}"
CSV_DIR="${CSV_DIR:-$SCRIPT_DIR/test-data/csv}"
POM="$SCRIPT_DIR/pom.xml"

GREEN='\033[0;32m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

GATEWAY_PROP="${GATEWAY_URL:+-Dgateway.url=$GATEWAY_URL}"

run_junit() {
  local label=$1
  local class=$2
  stage "$label"
  mvn failsafe:integration-test failsafe:verify -f "$POM" \
    $GATEWAY_PROP \
    -Dit.test="$class" \
    -q
  ok "$label passed"
}

run_class() {
  local class=$1
  local label=$2
  stage "$label"
  mvn exec:java -f "$POM" \
    -Dexec.mainClass="$class" \
    $GATEWAY_PROP \
    -q
  ok "$label passed"
}

run_admin() {
  stage "Admin import endpoints"
  [[ -d "$CSV_DIR" ]] \
    || fail "Test CSV data not found at $CSV_DIR — run: ./healthcare-infra/synthea/run-synthea.sh test-data <n>"
  mvn failsafe:integration-test failsafe:verify -f "$POM" \
    $GATEWAY_PROP \
    -Dtest.csv.dir="$CSV_DIR" \
    -Dit.test="admin.AdminImportIT" \
    -q
  ok "Admin import tests passed"
}

if [[ $# -eq 0 ]]; then
  echo ""
  echo "Usage: $0 <test|all> [test2 ...]"
  echo ""
  echo "Tests: seed  auth  register  patient  provider  admin  all"
  echo ""
  echo "Gateway URL: $GATEWAY_URL"
  echo "Override:    GATEWAY_URL=https://... $0 all"
  echo "CSV dir:     CSV_DIR=/path/to/csv $0 admin  (default: integration_tests/test-data/csv)"
  echo ""
  exit 0
fi

echo "Gateway: $GATEWAY_URL"

stage "Compiling integration tests"
mvn compile -f "$POM" -q
ok "Compiled"

for arg in "$@"; do
  case $arg in
    seed)     run_class "util.SeedAccounts"             "Seed / verify accounts" ;;
    auth)     run_junit "Auth endpoints"                "auth.AuthIT" ;;
    register)
      run_junit "Register patient endpoint"   "auth.RegisterPatientIT"
      run_junit "Register provider endpoint"  "auth.RegisterProviderIT"
      ;;
    patient)
      run_junit "Patient profile endpoints"        "patient.PatientProfileIT"
      ;;
    provider)
      run_junit "Provider profile endpoints"       "provider.ProviderProfileIT"
      ;;
    admin)
      run_admin
      ;;
    all)
      run_class "util.SeedAccounts"                "Seed / verify accounts"
      run_junit "Auth endpoints"                   "auth.AuthIT"
      run_junit "Register patient endpoint"        "auth.RegisterPatientIT"
      run_junit "Register provider endpoint"       "auth.RegisterProviderIT"
      run_junit "Patient profile endpoints"        "patient.PatientProfileIT"
      run_junit "Provider profile endpoints"       "provider.ProviderProfileIT"
      run_admin
      ;;
    *) fail "Unknown test: $arg  (known: seed auth register patient provider admin all)" ;;
  esac
done

echo -e "\n${GREEN}=== All tests passed ===${NC}\n"
