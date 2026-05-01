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
#   register      — auth.RegisterPatientIT
#   patient       — patient.PatientProfileIT + encounter.PatientEncountersIT
#   provider      — provider.ProviderProfileIT + encounter.ProviderEncountersIT
#   all           — run all tests in order
#
# Migration status:
#   auth     → JUnit 5 (Failsafe)
#   others   → legacy main() style (exec plugin), to be migrated
#
# Examples:
#   ./integration_tests/run-it.sh auth
#   ./integration_tests/run-it.sh patient provider
#   ./integration_tests/run-it.sh all
# =============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
GATEWAY_URL="${GATEWAY_URL:-https://gateway-dev-824144893232.us-west1.run.app}"
POM="$SCRIPT_DIR/pom.xml"

GREEN='\033[0;32m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

run_junit() {
  local label=$1
  local class=$2
  stage "$label"
  mvn failsafe:integration-test failsafe:verify -f "$POM" \
    -Dgateway.url="$GATEWAY_URL" \
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
    -Dgateway.url="$GATEWAY_URL" \
    -q
  ok "$label passed"
}

if [[ $# -eq 0 ]]; then
  echo ""
  echo "Usage: $0 <test|all> [test2 ...]"
  echo ""
  echo "Tests: seed  auth  register  patient  provider  all"
  echo ""
  echo "Gateway URL: $GATEWAY_URL"
  echo "Override:    GATEWAY_URL=https://... $0 all"
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
    register) run_junit "Register patient endpoint"    "auth.RegisterPatientIT" ;;
    patient)
      run_junit "Patient profile endpoints"        "patient.PatientProfileIT"
      run_junit "Patient encounter endpoints"      "encounter.PatientEncountersIT"
      ;;
    provider)
      run_junit "Provider profile endpoints"       "provider.ProviderProfileIT"
      run_junit "Provider encounter endpoints"     "encounter.ProviderEncountersIT"
      ;;
    all)
      run_class "util.SeedAccounts"                "Seed / verify accounts"
      run_junit "Auth endpoints"                   "auth.AuthIT"
      run_junit "Register patient endpoint"        "auth.RegisterPatientIT"
      run_junit "Patient profile endpoints"        "patient.PatientProfileIT"
      run_junit "Patient encounter endpoints"      "encounter.PatientEncountersIT"
      run_junit "Provider profile endpoints"       "provider.ProviderProfileIT"
      run_junit "Provider encounter endpoints"     "encounter.ProviderEncountersIT"
      ;;
    *) fail "Unknown test: $arg  (known: seed auth register patient provider all)" ;;
  esac
done

echo -e "\n${GREEN}=== All tests passed ===${NC}\n"
