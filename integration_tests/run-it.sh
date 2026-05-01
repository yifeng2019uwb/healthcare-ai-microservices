#!/bin/bash
# =============================================================================
# run-it.sh — Run integration tests against the deployed gateway
# =============================================================================
# Usage:
#   ./integration_tests/run-it.sh <test|all> [test2 ...]
#
# Tests:
#   seed          — verify test accounts are reachable
#   register      — auth.RegisterPatientIT
#   auth          — auth.AuthIT (login, refresh, logout)
#   patient       — encounter.PatientEncountersIT + patient.PatientProfileIT
#   provider      — encounter.ProviderEncountersIT + provider.ProviderProfileIT
#   all           — run all tests in order
#
# Examples:
#   ./integration_tests/run-it.sh seed
#   ./integration_tests/run-it.sh patient provider
#   ./integration_tests/run-it.sh all
# =============================================================================

set -e

GATEWAY_URL="${GATEWAY_URL:-https://gateway-dev-824144893232.us-west1.run.app}"
POM="integration_tests/pom.xml"

GREEN='\033[0;32m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
ok()    { echo -e "${GREEN}✓ $1${NC}"; }
fail()  { echo -e "${RED}✗ $1${NC}"; exit 1; }
stage() { echo -e "\n${BLUE}=== $1 ===${NC}"; }

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
  echo "Tests: seed  register  auth  patient  provider  all"
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
    seed)     run_class "util.SeedAccounts"                  "Seed / verify accounts" ;;
    register) run_class "auth.RegisterPatientIT"             "Register patient (auth)" ;;
    auth)     run_class "auth.AuthIT"                        "Auth: login, refresh, logout" ;;
    patient)
      run_class "patient.PatientProfileIT"           "Patient profile endpoints"
      run_class "encounter.PatientEncountersIT"      "Patient encounter endpoints"
      ;;
    provider)
      run_class "provider.ProviderProfileIT"         "Provider profile endpoints"
      run_class "encounter.ProviderEncountersIT"     "Provider encounter endpoints"
      ;;
    all)
      run_class "util.SeedAccounts"                  "Seed / verify accounts"
      run_class "auth.AuthIT"                        "Auth: login, refresh, logout"
      run_class "patient.PatientProfileIT"           "Patient profile endpoints"
      run_class "encounter.PatientEncountersIT"      "Patient encounter endpoints"
      run_class "provider.ProviderProfileIT"         "Provider profile endpoints"
      run_class "encounter.ProviderEncountersIT"     "Provider encounter endpoints"
      ;;
    *) fail "Unknown test: $arg  (known: seed register auth patient provider all)" ;;
  esac
done

echo -e "\n${GREEN}=== All tests passed ===${NC}\n"
