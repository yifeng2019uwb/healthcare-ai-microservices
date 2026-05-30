# Integration Test Guide

Black-box RestAssured tests that run against the live deployed gateway.
Tests exercise the full stack: gateway → service → Supabase DB.

## Prerequisites

- Java 21 and Maven installed
- Gateway reachable at `http://localhost:8080` (default) or set `GATEWAY_URL`
- Test accounts seeded in the DB (see [Test Accounts](#test-accounts))

## Running Tests

```bash
cd integration_tests

# Verify test accounts are reachable before running suites
./run-it.sh seed

# Run all suites
./run-it.sh all

# Run specific suites
./run-it.sh auth
./run-it.sh register
./run-it.sh patient
./run-it.sh provider
./run-it.sh admin

# Run multiple suites
./run-it.sh auth patient provider

# Override gateway URL
GATEWAY_URL=http://<vm-ip>:8080 ./run-it.sh all
```

## Test Suites

| Suite | Class | Description |
|-------|-------|-------------|
| `seed` | `util.SeedAccounts` | Verifies test accounts are reachable |
| `auth` | `auth.AuthIT` | Login, refresh, logout flows |
| `register` | `auth.RegisterPatientIT` + `auth.RegisterProviderIT` | Registration validation and error paths |
| `patient` | `patient.PatientProfileIT` | Patient profile, encounters, conditions, allergies |
| `provider` | `provider.ProviderProfileIT` | Provider profile, patient list |
| `ai` | `ai.AiAnalysisIT` | Condition write + AI analysis request/read (excludes `@Tag("ai-live")`) |
| `ai-live` | `ai.AiAnalysisIT` | Full AI suite including live Gemini call (slow, costs API quota) |
| `admin` | `admin.AdminImportIT` | Synthea CSV import endpoints |

## Test Accounts

Defined in `integration_tests/util/TestAccounts.java`.

| Role | Username | Email | Notes |
|------|----------|-------|-------|
| Patient | `testpatient01` | `test01@example.com` | Jena102 Gislason620, DOB 1974-04-17 |
| Patient 2 | `testpatient02` | `test02@example.com` | Cross-patient isolation tests |
| Provider | `drDouglass` | `Douglass930@hospital.com` | Douglass930 Windler79, org NAVOS |
| Admin | `admin123` | — | Synthea import tests |

Override any account via system property:
```bash
GATEWAY_URL=... ./run-it.sh auth \
  -Dtest.provider.username=other_provider \
  -Dtest.provider.email=other@hospital.com
```

## Stateful Tests

Some happy-path registration tests are `@Disabled` — they register an account and cannot be re-run once the record is linked. To run them:

1. Enable the `@Disabled` test in the relevant `Register*IT.java`
2. Run: `./run-it.sh register`
3. Re-disable immediately after a successful run

Affected tests:
- `RegisterPatientIT.register_withValidUnregisteredPatient_returns201`
- `RegisterProviderIT.register_withValidUnregisteredProvider_returns201`

## Admin Suite — Extra Setup

The `admin` suite requires Synthea CSV data:

```bash
# Generate test CSV data (n = number of patients)
./healthcare-infra/synthea/run-synthea.sh test-data <n>

# Then run admin tests
CSV_DIR=./integration_tests/test-data/csv ./run-it.sh admin
```

## Structure

```
integration_tests/
├── run-it.sh                    # main runner
├── pom.xml
└── src/test/java/
    ├── util/
    │   ├── BaseIT.java          # RestAssured base — reads GATEWAY_URL
    │   ├── TestAccounts.java    # credential constants
    │   ├── LoginHelper.java     # asPatient(), asProvider(), withToken()
    │   ├── ApiPaths.java        # API path constants
    │   └── SeedAccounts.java    # seed verification
    ├── auth/
    │   ├── AuthIT.java
    │   ├── RegisterPatientIT.java
    │   └── RegisterProviderIT.java
    ├── patient/
    │   └── PatientProfileIT.java
    ├── provider/
    │   └── ProviderProfileIT.java
    ├── ai/
    │   └── AiAnalysisIT.java
    └── admin/
        └── AdminImportIT.java
```
