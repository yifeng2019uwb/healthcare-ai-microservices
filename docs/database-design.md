# Database Design

> Version: 2.0 | Last Updated: May 2026

---

## Overview

Supabase PostgreSQL. All services share a single connection (no per-service DB users).
Schema managed via SQL files in `healthcare-infra/schema/sql/` — deployed with `run-schema.sh`.

**Database**: Supabase managed PostgreSQL
**Connection**: injected via Docker Compose env vars — `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`
**DDL mode**: `validate` in all service configs — Hibernate never modifies the schema

---

## Data Source

All clinical tables (patients, organizations, providers, encounters, conditions,
allergies) are populated from Synthea synthetic patient data — ~200 Washington
state patients. No real patient data.

See `healthcare-infra/synthea/` for generation and loading scripts.

---

## Patient Record Matching

### Phase 1 — MRN
`mrn` column in the `patients` table. Service-generated (e.g. `MRN-000001`), given
to the patient by their provider. Used as the primary matching key at registration.

MRN is currently generated with `Random` — tracked in tech debt as a future fix
to use a DB sequence (collision-safe, like `provider_code`).

### Provider Matching
Provider registration uses `organization_name` + full name (`findByNameAndOrganizationId`).
Backed by composite index `(organization_id, name)` on the providers table.

---

## Table Ownership and Access

| Table | Owner Service | Write | Read |
|-------|---------------|-------|------|
| `users` | auth-service | auth-service only | auth-service only |
| `patients` | patient-service | patient (own) + provider (create) | patient-service + provider-service |
| `organizations` | provider-service | provider-service | all services |
| `providers` | provider-service | provider-service | all services |
| `encounters` | appointment-service | appointment-service | all services |
| `conditions` | patient-service | provider only | patient-service + provider-service |
| `allergies` | patient-service | provider only | patient-service + provider-service |
| `audit_logs` | all services | all services | admin only |

### Access Rules

- **Patient** — reads/updates own profile only; reads own conditions, allergies, encounters; cannot write clinical data
- **Provider** — reads patient data where encounter exists; writes clinical data (conditions, allergies) after visit
- **Auth service** — exclusive access to users table; no other service touches credentials
- **Audit logs** — every service writes; no service reads (admin/monitoring only)

---

## Schema Files

All SQL files in `healthcare-infra/schema/sql/` — idempotent (`CREATE TABLE IF NOT EXISTS`):

| File | Tables |
|------|--------|
| `users.sql` | users |
| `patients.sql` | patients |
| `organizations.sql` | organizations |
| `providers.sql` | providers |
| `encounters.sql` | encounters |
| `conditions.sql` | conditions |
| `allergies.sql` | allergies |
| `audit_logs.sql` | audit_logs |

---

## Table Definitions

### `users`
Owner: auth-service. Login credentials and role. Not PHI.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK | |
| username | VARCHAR(100) UNIQUE NOT NULL | |
| email | VARCHAR(255) UNIQUE NOT NULL | |
| password_hash | VARCHAR(255) NOT NULL | BCrypt |
| role | VARCHAR(20) NOT NULL | PATIENT, PROVIDER, ADMIN |
| is_active | BOOLEAN NOT NULL DEFAULT true | |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

### `patients`
Owner: patient-service. Synthea patients.csv + auth linkage.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK | Set by service (Synthea UUID on import, `UUID.randomUUID()` for new records) |
| auth_id | UUID UNIQUE | FK to users.id — null until registered |
| mrn | VARCHAR(20) UNIQUE | Service-generated (e.g. MRN-000001). Given to patient for registration. |
| birthdate | DATE NOT NULL | |
| deathdate | DATE | Null if alive |
| prefix | VARCHAR(10) | |
| first_name | VARCHAR(100) NOT NULL | |
| middle_name | VARCHAR(100) | |
| last_name | VARCHAR(100) NOT NULL | |
| suffix | VARCHAR(10) | |
| maiden | VARCHAR(100) | |
| marital | VARCHAR(1) | S/M/D/W |
| race | VARCHAR(50) | |
| ethnicity | VARCHAR(50) | |
| gender | VARCHAR(10) NOT NULL | M/F |
| birthplace | VARCHAR(255) | |
| address | VARCHAR(255) | |
| city | VARCHAR(100) | |
| state | VARCHAR(50) | |
| county | VARCHAR(100) | |
| fips | VARCHAR(20) | |
| zip | VARCHAR(10) | |
| lat | DECIMAL(10,6) | System derived |
| lon | DECIMAL(10,6) | System derived |
| healthcare_expenses | DECIMAL(12,2) | |
| healthcare_coverage | DECIMAL(12,2) | |
| income | INTEGER | |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

### `organizations`
Owner: provider-service. Synthea organizations.csv.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK | Set by service (Synthea UUID on import) |
| name | VARCHAR(255) NOT NULL | |
| address | VARCHAR(255) NOT NULL | |
| city | VARCHAR(100) NOT NULL | |
| state | VARCHAR(50) NOT NULL | |
| zip | VARCHAR(20) NOT NULL | |
| phone | VARCHAR(50) | |
| lat | DECIMAL(10,6) | System derived |
| lon | DECIMAL(10,6) | System derived |
| revenue | DECIMAL(12,2) | Synthea metric, nullable |
| utilization | INTEGER | Synthea metric, nullable |

### `providers`
Owner: provider-service. Synthea providers.csv + auth linkage.
Address removed — derived from organization_id JOIN.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK | Set by service (Synthea UUID on import) |
| organization_id | UUID NOT NULL FK → organizations | |
| auth_id | UUID UNIQUE | FK to users.id — null until registered |
| provider_code | VARCHAR(20) UNIQUE | Service-generated (e.g. PRV-000001). Given to provider for registration. |
| name | VARCHAR(255) NOT NULL | |
| gender | VARCHAR(1) | M/F |
| speciality | VARCHAR(100) | Synthea spelling |
| encounters | INTEGER | Synthea metric, nullable |
| procedures | INTEGER | Synthea metric, nullable |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

### `encounters`
Owner: appointment-service. Synthea encounters.csv.
organization_id kept as snapshot — org/provider may change after encounter.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK | Set by service (Synthea UUID on import) |
| patient_id | UUID NOT NULL FK → patients | |
| provider_id | UUID NOT NULL FK → providers | |
| organization_id | UUID NOT NULL FK → organizations | Snapshot at time of encounter |
| payer_id | VARCHAR(36) | Phase 2: FK to payers when table exists |
| start_time | TIMESTAMPTZ NOT NULL | |
| stop_time | TIMESTAMPTZ | |
| encounter_class | VARCHAR(50) | ambulatory, emergency, inpatient, wellness |
| code | VARCHAR(20) | SNOMED-CT |
| description | VARCHAR(255) | |
| base_cost | DECIMAL(10,2) | |
| total_cost | DECIMAL(10,2) | |
| payer_coverage | DECIMAL(10,2) | Phase 2 |
| reason_code | VARCHAR(20) | SNOMED-CT |
| reason_desc | VARCHAR(255) | |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

### `conditions`
Owner: patient-service. Write: provider only. Synthea conditions.csv.

| Column | Type | Notes |
|--------|------|-------|
| patient_id | UUID NOT NULL FK → patients | |
| encounter_id | UUID NOT NULL FK → encounters | |
| start_date | DATE NOT NULL | |
| stop_date | DATE | Null if ongoing |
| system | VARCHAR(20) NOT NULL DEFAULT 'SNOMED-CT' | |
| code | VARCHAR(20) NOT NULL | SNOMED-CT |
| description | VARCHAR(255) NOT NULL | |
| PRIMARY KEY | (patient_id, encounter_id, code) | Composite |

### `allergies`
Owner: patient-service. Write: provider only. Synthea allergies.csv.

| Column | Type | Notes |
|--------|------|-------|
| patient_id | UUID NOT NULL FK → patients | |
| encounter_id | UUID NOT NULL FK → encounters | |
| start_date | DATE NOT NULL | |
| stop_date | DATE | |
| code | VARCHAR(20) NOT NULL | |
| system | VARCHAR(20) | RxNorm or SNOMED-CT |
| description | VARCHAR(255) NOT NULL | |
| allergy_type | VARCHAR(20) | allergy / intolerance |
| category | VARCHAR(20) | environment, food, drug |
| reaction1 | VARCHAR(20) | |
| description1 | VARCHAR(255) | |
| severity1 | VARCHAR(10) | MILD, MODERATE, SEVERE |
| reaction2 | VARCHAR(20) | |
| description2 | VARCHAR(255) | |
| severity2 | VARCHAR(10) | |
| notes | TEXT | Additional reactions beyond Synthea 2-reaction limit |
| PRIMARY KEY | (patient_id, encounter_id, code) | Composite |

### `audit_logs`
HIPAA Security Rule 45 CFR § 164.312(b). Append only — no DELETE.
Retention: 6 years minimum.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID PK DEFAULT gen_random_uuid() | |
| auth_id | VARCHAR(128) | Who made the request |
| user_role | VARCHAR(20) | PATIENT, PROVIDER — minimum necessary rule |
| action | VARCHAR(10) NOT NULL | READ, CREATE, UPDATE — no DELETE (HIPAA) |
| resource_type | VARCHAR(50) NOT NULL | patients, encounters, conditions etc |
| resource_id | UUID | Specific record accessed |
| outcome | VARCHAR(10) NOT NULL | SUCCESS, FAILURE |
| source_ip | INET | |
| user_agent | TEXT | |
| created_at | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

---

## Indexes

Every DAO query method has a corresponding DB index. Key composite indexes:

| Index | Table | Columns | Reason |
|-------|-------|---------|--------|
| idx_providers_org_name | providers | (organization_id, name) | Provider registration lookup |
| idx_encounters_provider_time | encounters | (provider_id, start_time DESC) | Provider encounter history |
| idx_encounters_patient_time | encounters | (patient_id, start_time DESC) | Patient encounter history |
| idx_encounters_provider_patient | encounters | (provider_id, patient_id) | Provider's patient list |
| idx_allergies_encounter | allergies | (encounter_id) | Allergies by encounter |
| idx_conditions_encounter | conditions | (encounter_id) | Conditions by encounter |

---

## Design Decisions

**Why the service sets UUIDs instead of the DB?**
Service layer calls `UUID.randomUUID()` for new records and uses the Synthea UUID for
imported records. DB enforces `PRIMARY KEY` uniqueness only. This keeps UUID generation
predictable and testable, and lets import preserve Synthea's cross-table FK references.

MRN and provider_code follow the same principle — service-generated, DB enforces uniqueness.

**Why auth_id is not a strict FK to users?**
Microservices pattern — no FK constraints crossing service boundaries.
Services communicate via JWT claims, not shared DB constraints.

**Why organization_id is kept in encounters as a snapshot?**
Provider or org data may change after an encounter. The snapshot preserves what was
true at the time of the visit — correct healthcare data modeling.

**Why no DELETE on audit_logs?**
HIPAA prohibits deleting PHI access records. Audit logs are append-only by design.

**Why provider writes conditions and allergies but doesn't own the tables?**
Ownership = who manages the table structure.
Write access = who can insert/update rows at runtime.
Provider writes clinical data after visits; patient-service owns the schema.

**Why all services share one DB connection?**
Running on a single VM with Supabase. Per-service DB users add operational complexity
with no practical blast radius benefit at this scale. Access control is enforced at
the application layer (service boundaries + JWT claims).
