# Synthea Data Import Design

> Version: 1.1 | Last Updated: 2026-05-13

---

## Overview

Replace the direct-to-DB `psql COPY` load in `run-synthea.sh` with API-driven import.
Synthea CSV generation stays unchanged. A new `load-api` command uploads each CSV file
directly to the provider-service ADMIN endpoints. Java parses the CSV into objects and
inserts them in FK dependency order.

**Why API instead of direct DB:**
- Data passes through the same service layer as all other writes (validation, audit)
- No need to open direct DB access from a local machine
- Import is testable via the same integration tests as normal service calls

---

## Scope

**In scope:**
- Upload Synthea-generated CSV files to provider-service via ADMIN API endpoints
- Java CSV parsing and mapping to DB entities (no external tooling)
- FK-ordered import: organizations → patients → providers → encounters → conditions → allergies
- Idempotent insert — safe to re-run against existing data
- Updated `run-synthea.sh` with `load-api` command

**Out of scope :**
- Detailed per-row error reporting (duplicates list, invalid list) — tracked as tech debt
- Dry-run mode
- Any changes to Synthea generation step

---

## End-to-End Flow

```
run-synthea.sh generate        → synthea-with-dependencies.jar → output/csv/*.csv
run-synthea.sh load-api        → 1. POST /api/auth/login (ADMIN) → get JWT
                                  2. curl -F file=@<table>.csv in FK order
                                  3. provider-service: parse CSV → objects → INSERT ON CONFLICT DO NOTHING
```

---

## Prerequisites

| Requirement | Details |
|---|---|
| ADMIN account | Must exist in `users` table with `role = ADMIN` |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Set in `.env` or passed as env vars |
| `GATEWAY_URL` | Base URL of the gateway (e.g. `https://gateway-xxx.run.app`) |
| `curl` + `jq` | Standard tools — no Python3 or custom conversion needed |

---

## Import Order (FK Dependencies)

| Step | Endpoint | Writes to | Depends on |
|---|---|---|---|
| 1 | `POST /api/admin/import/organizations` | organizations | — |
| 2 | `POST /api/admin/import/patients` | patients | — |
| 3 | `POST /api/admin/import/providers` | providers | organizations |
| 4 | `POST /api/admin/import/encounters` | encounters | patients, organizations, providers |
| 5 | `POST /api/admin/import/conditions` | conditions | patients, encounters |
| 6 | `POST /api/admin/import/allergies` | allergies | patients, encounters |

---

## Idempotency

All import endpoints use `INSERT ... ON CONFLICT DO NOTHING`.
Safe to run multiple times — duplicate rows are silently skipped.

---

## Request Format

All endpoints accept `Content-Type: multipart/form-data` with a single `file` part
containing the Synthea CSV file (header row included, unchanged from Synthea output).

```bash
curl -X POST \
  -H "Authorization: Bearer $JWT" \
  -F "file=@output/csv/patients.csv" \
  "$GATEWAY_URL/api/admin/import/patients"
```

Java parses the CSV using OpenCSV. One mapper method per table maps CSV rows to the
internal entity. Unknown columns in the CSV are ignored.

---

## CSV Column Mapping

### organizations.csv

| CSV column | Java field | Type |
|---|---|---|
| Id | id | UUID |
| NAME | name | String |
| ADDRESS | address | String |
| CITY | city | String |
| STATE | state | String |
| ZIP | zip | String |
| LAT | lat | BigDecimal |
| LON | lon | BigDecimal |
| PHONE | phone | String |
| REVENUE | revenue | BigDecimal |
| UTILIZATION | utilization | Integer |

### patients.csv

| CSV column | Java field | Type |
|---|---|---|
| Id | id | UUID |
| BIRTHDATE | birthdate | LocalDate |
| DEATHDATE | deathdate | LocalDate (nullable) |
| SSN | ssn | String |
| DRIVERS | drivers | String |
| PASSPORT | passport | String |
| PREFIX | prefix | String |
| FIRST | firstName | String |
| MIDDLE | middleName | String |
| LAST | lastName | String |
| SUFFIX | suffix | String |
| MAIDEN | maiden | String |
| MARITAL | marital | String |
| RACE | race | String |
| ETHNICITY | ethnicity | String |
| GENDER | gender | String |
| BIRTHPLACE | birthplace | String |
| ADDRESS | address | String |
| CITY | city | String |
| STATE | state | String |
| COUNTY | county | String |
| FIPS | fips | String |
| ZIP | zip | String |
| LAT | lat | BigDecimal |
| LON | lon | BigDecimal |
| HEALTHCARE_EXPENSES | healthcareExpenses | BigDecimal |
| HEALTHCARE_COVERAGE | healthcareCoverage | BigDecimal |
| INCOME | income | Integer |

### providers.csv

| CSV column | Java field | Type |
|---|---|---|
| Id | id | UUID |
| ORGANIZATION | organizationId | UUID |
| NAME | name | String |
| GENDER | gender | String |
| SPECIALITY | speciality | String |
| ENCOUNTERS | encounters | Integer |
| PROCEDURES | procedures | Integer |

_(address, city, state, zip, lat, lon columns in CSV are ignored — not in schema)_

### encounters.csv

| CSV column | Java field | Type |
|---|---|---|
| Id | id | UUID |
| START | startTime | OffsetDateTime |
| STOP | stopTime | OffsetDateTime |
| PATIENT | patientId | UUID |
| ORGANIZATION | organizationId | UUID |
| PROVIDER | providerId | UUID |
| PAYER | payerId | String |
| ENCOUNTERCLASS | encounterClass | String |
| CODE | code | String |
| DESCRIPTION | description | String |
| BASE_ENCOUNTER_COST | baseCost | BigDecimal |
| TOTAL_CLAIM_COST | totalCost | BigDecimal |
| PAYER_COVERAGE | payerCoverage | BigDecimal |
| REASONCODE | reasonCode | String |
| REASONDESCRIPTION | reasonDesc | String |

### conditions.csv

| CSV column | Java field | Type |
|---|---|---|
| START | startDate | LocalDate |
| STOP | stopDate | LocalDate (nullable) |
| PATIENT | patientId | UUID |
| ENCOUNTER | encounterId | UUID |
| SYSTEM | system | String |
| CODE | code | String |
| DESCRIPTION | description | String |

### allergies.csv

| CSV column | Java field | Type |
|---|---|---|
| START | startDate | LocalDate |
| STOP | stopDate | LocalDate (nullable) |
| PATIENT | patientId | UUID |
| ENCOUNTER | encounterId | UUID |
| CODE | code | String |
| SYSTEM | system | String |
| DESCRIPTION | description | String |
| TYPE | allergyType | String |
| CATEGORY | category | String |
| REACTION1 | reaction1 | String |
| DESCRIPTION1 | description1 | String |
| SEVERITY1 | severity1 | String |
| REACTION2 | reaction2 | String |
| DESCRIPTION2 | description2 | String |
| SEVERITY2 | severity2 | String |

---

## Response

All import endpoints return the same structure:

```json
{
  "imported": 487,
  "skipped": 13,
  "total": 500
}
```

- `imported` — rows inserted
- `skipped` — rows skipped due to conflict (already exist)
- `total` — rows parsed from CSV

> **Tech debt:** Response is intentionally minimal for now. Future improvement should
> include a `duplicates` list (IDs of skipped rows) and an `invalid` list (row number +
> reason) so the caller knows exactly what was rejected without re-running a diff.

HTTP 400 if the CSV is malformed (unparseable header or row). All rows are inserted in
one transaction — any parse error rolls back the whole file.

---

## Script Changes (`run-synthea.sh`)

```
./run-synthea.sh generate      # unchanged — generate CSVs via Synthea JAR
./run-synthea.sh load-api      # new — upload CSVs to API in FK order
./run-synthea.sh all-api       # generate + load-api
./run-synthea.sh load          # kept — direct psql (local dev / emergency recovery)
```

### `load-api` steps

1. Read `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `GATEWAY_URL` from env
2. `POST $GATEWAY_URL/api/auth/login` → extract JWT with `jq`
3. For each table in FK order: `curl -F file=@<table>.csv` with `Authorization: Bearer $JWT`
4. Print per-table summary (imported / skipped / total)
5. Exit non-zero if any upload returns non-2xx

---

## Files to Create / Change

| File | Change |
|---|---|
| `services/provider-service/.../AdminImportController.java` | New — 6 POST multipart endpoints |
| `services/provider-service/.../AdminImportService.java` | CSV parse → batch insert `ON CONFLICT DO NOTHING` |
| `services/provider-service/.../csv/CsvMapper.java` | One static method per table: `List<row>` from `MultipartFile` |
| `services/provider-service/pom.xml` | Add OpenCSV dependency |
| `healthcare-infra/synthea/run-synthea.sh` | Add `load-api` and `all-api` commands |

