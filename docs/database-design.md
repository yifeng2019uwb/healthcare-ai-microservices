# Database Design

> Version: 1.0 | Last Updated: March 2026
> Previous versions archived in `docs/archive/`

---

## Overview

Single Cloud SQL PostgreSQL 15 instance, one database (`healthcare`), multiple
tables. Each service connects with its own DB user — permissions enforced at
the PostgreSQL role level, not just application code.

**Database instance**: `healthcare-db-dev` (GCP us-west1)
**Database name**: `healthcare`
**Infrastructure**: See `healthcare-infra/terraform/cloud_sql.tf`

---

## Data Source

All clinical tables (patients, organizations, providers, encounters, conditions,
allergies) are populated from Synthea synthetic patient data — 200 Washington
state patients. No real patient data.

See `healthcare-infra/synthea/` for generation and loading scripts.

---

## Patient Record Matching

### Phase 1 — Best Effort Match
```
first_name + last_name + birthdate + gender
```
Limitation — not guaranteed unique for large datasets. Two patients
could share the same name, birthdate, and gender.

### Phase 2 — MRN (Medical Record Number)
`mrn` column already added to `patients` table as nullable — ready
for Phase 2 without schema change.

MRN is system-generated, non-sensitive, given to patient by provider.
More reliable than name+birthdate matching.

### Future — SSN Matching
Synthea generates unique fake SSNs. Real healthcare systems use SSN
for patient matching. Requires encryption at rest — not implemented
in Phase 1.

---

## Table Ownership and Access

| Table | Owner Service | Write Access | Read Access |
|---|---|---|---|
| `users` | auth-service | auth-service only | auth-service only |
| `patients` | patient-service | patient (own profile) + provider (create) | patient-service + provider-service |
| `organizations` | provider-service | provider-service only | all services |
| `providers` | provider-service | provider-service only | all services |
| `encounters` | appointment-service | appointment-service (Phase 2) | all services |
| `conditions` | patient-service | Phase 2 | patient-service + provider-service |
| `allergies` | patient-service | Phase 2 | patient-service + provider-service |
| `audit_logs` | all services | all services | admin only |

### Access Rules

- **Patient** — reads/updates own profile only; reads own conditions, allergies,
  encounters; cannot write clinical data
- **Provider** — reads patient data where encounter exists; writes clinical data
  (conditions, allergies) after visit; updates encounter records
- **Auth service** — exclusive access to users table; no other service can read
  or write credentials
- **Audit logs** — every service writes; no service reads (admin/monitoring only)

### Why Provider Writes conditions and allergies

Provider is a qualified clinician — they add diagnoses and allergies after a
patient visit. Patient cannot self-diagnose or add their own medical conditions.
This follows the HIPAA minimum necessary rule — each role only performs actions
their job requires.

---

## PostgreSQL Roles and Permissions

Each service connects with its own DB user assigned a role with restricted
table permissions. Even if one service is compromised, the attacker can only
access that service's permitted tables.

See `healthcare-infra/schema/sql/permissions.sql` for full SQL.

### Role Summary

```
auth_role
    CRUD  → users

patient_role
    CRU   → patients
    CRU   → conditions
    CRU   → allergies
    R     → providers, organizations, encounters
    C     → audit_logs

provider_role
    CRUD  → providers, organizations
    CRU   → conditions, allergies, encounters
    R     → patients
    C     → audit_logs

appointment_role
    CRU   → encounters
    R     → patients, providers, organizations
    C     → audit_logs
```

### Service DB Users

| Service | DB User | Role Assigned | Secret Manager Key |
|---|---|---|---|
| auth-service | `auth_service_user` | `auth_role` | `db-password-auth-service` |
| patient-service | `patient_service_user` | `patient_role` | `db-password-patient-service` |
| provider-service | `provider_service_user` | `provider_role` | `db-password-provider-service` |
| appointment-service | `appointment_service_user` | `appointment_role` | `db-password-appointment-service` |

---

## Schema Files

All SQL files in `healthcare-infra/schema/sql/`:

| File | Tables | Phase |
|---|---|---|
| `users.sql` | users | 1 |
| `patients.sql` | patients | 1 |
| `organizations.sql` | organizations | 1 |
| `providers.sql` | providers | 1 |
| `encounters.sql` | encounters | 1 |
| `conditions.sql` | conditions | 1 |
| `allergies.sql` | allergies | 1 |
| `audit_logs.sql` | audit_logs | 1 |
| `permissions.sql` | roles + grants | 1 |
| `medications.sql` | medications | 2 |
| `observations.sql` | observations | 2 |

---

## Table Definitions

### `users`
Owner: auth-service. Stores login credentials and role. Not PHI.

| Column | Type | Notes |
|---|---|---|
| id | UUID PK DEFAULT gen_random_uuid() | |
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
|---|---|---|
| id | UUID PK | Synthea patient UUID |
| auth_id | UUID UNIQUE | FK to users.id — null until registered |
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
|---|---|---|
| id | UUID PK | Synthea org UUID |
| name | VARCHAR(255) NOT NULL | |
| address | VARCHAR(255) NOT NULL | Required for registration |
| city | VARCHAR(100) NOT NULL | |
| state | VARCHAR(50) NOT NULL | |
| zip | VARCHAR(20) NOT NULL | |
| phone | VARCHAR(50) | |
| lat | DECIMAL(10,6) | System derived via geocoding |
| lon | DECIMAL(10,6) | System derived via geocoding |
| revenue | DECIMAL(12,2) | Synthea metric, nullable |
| utilization | INTEGER | Synthea metric, nullable |

### `providers`
Owner: provider-service. Synthea providers.csv + auth linkage.
Address removed — derived from organization_id JOIN.

| Column | Type | Notes |
|---|---|---|
| id | UUID PK | Synthea provider UUID |
| organization_id | UUID NOT NULL FK → organizations | |
| auth_id | UUID UNIQUE | FK to users.id — null until registered |
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
|---|---|---|
| id | UUID PK | Synthea encounter UUID |
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
|---|---|---|
| patient_id | UUID NOT NULL FK → patients | |
| encounter_id | UUID NOT NULL FK → encounters | |
| start_date | DATE NOT NULL | |
| stop_date | DATE | Null if ongoing |
| system | VARCHAR(20) NOT NULL DEFAULT 'SNOMED-CT' | Kept for future data sources |
| code | VARCHAR(20) NOT NULL | SNOMED-CT |
| description | VARCHAR(255) NOT NULL | |
| PRIMARY KEY | (patient_id, encounter_id, code) | Composite |

### `allergies`
Owner: patient-service. Write: provider only. Synthea allergies.csv.

| Column | Type | Notes |
|---|---|---|
| patient_id | UUID NOT NULL FK → patients | |
| encounter_id | UUID NOT NULL FK → encounters | |
| start_date | DATE NOT NULL | |
| stop_date | DATE | |
| code | VARCHAR(20) NOT NULL | |
| system | VARCHAR(20) | RxNorm or SNOMED-CT or Unknown |
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
|---|---|---|
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

## Phase 2 Tables

| Table | Owner | Notes |
|---|---|---|
| `medications` | patient-service | Synthea medications.csv — for Vertex AI |
| `observations` | patient-service | Synthea observations.csv — for Vertex AI |
| `payers` | appointment-service | Synthea payers.csv — billing/insurance |

---

## Design Decisions

**Why one database, multiple tables (not separate databases)?**
Cost — one Cloud SQL instance, one database. Access control enforced at
PostgreSQL role level per service user, not by separate databases.

**Why auth_id is VARCHAR in patients/providers, not FK to users?**
Microservices pattern — no FK constraints crossing service boundaries.
Services communicate via JWT claims, not shared DB constraints.

**Why organization_id kept in encounters as snapshot?**
Provider or org data may change after an encounter. The snapshot preserves
what was true at the time of the visit — correct healthcare data modeling.

**Why no DELETE permission on audit_logs?**
HIPAA prohibits deleting PHI access records. Audit logs are append-only
by design and policy.

**Why provider writes conditions and allergies but doesn't own the table?**
Ownership = who manages the table structure and schema.
Write access = who can insert/update rows at runtime.
Provider writes clinical data after visits but patient-service owns the schema.