# Healthcare AI Platform — Master Design Document

> **Single source of truth.** Replaces all previous design docs.
> Version: 2.0 | Status: Active | Last Updated: March 2026

---

## 1. Project Overview

### What This Is

A cloud-native healthcare API platform built on GCP that serves realistic synthetic patient data,
supports appointment management, and uses Vertex AI Gemini for clinical insights. The platform
demonstrates production-grade cloud security practices applicable to any healthcare organization —
federal agencies, health systems, insurance companies, or health tech startups.

### Why It Matters

- **Transferable skills**: GCP security patterns, FHIR data, OAuth 2.0/RBAC apply across all
  healthcare organizations and cloud security roles
- **Real data model**: Built on Synthea synthetic data — the same tool used by CMS, ONC,
  and federal health IT programs
- **Security-first**: Cloud Armor WAF, GCP Secret Manager, Firebase Auth, audit logging,
  and STRIDE threat modeling are core, not afterthoughts

### Target Roles This Demonstrates

Backend Engineer, Cloud/DevOps Engineer, Security Engineer, Federal IT (GS-2210 series)

---

## 2. Tech Stack

| Layer | Technology | Reason |
|---|---|---|
| **Cloud** | GCP (us-west1) | 90-day $300 free tier, active account |
| **Compute** | Cloud Run | Scales to zero, no idle cost, container-native |
| **Database** | Cloud SQL PostgreSQL 15 | Managed, VPC-private, GCP-native IAM |
| **Authentication** | Firebase Auth + GCP Identity Platform | OAuth 2.0 / OIDC, free tier generous |
| **Secrets** | GCP Secret Manager | No credentials in config files or env vars |
| **WAF** | Cloud Armor | OWASP Top 10 blocking at load balancer |
| **Audit** | Cloud Audit Logs + Cloud Logging | Who accessed what patient data and when |
| **AI** | Vertex AI Gemini | Patient risk analysis, clinical summarization |
| **IaC** | Terraform (GCP provider) | Reuses existing HCL structure, just new provider |
| **CI/CD** | GitHub Actions + Workload Identity Federation | Keyless GCP auth — no credentials stored anywhere |
| **Services** | Spring Boot 3.2 / Java 17 | Existing codebase |
| **Data** | Synthea synthetic data (CSV) | Realistic FHIR-aligned patient records |

---

## 3. Data Source — Synthea

### What Synthea Is

Synthea is an open-source synthetic patient generator from MITRE. It produces realistic but not
real patient data covering complete medical histories including demographics, conditions,
medications, allergies, encounters, observations, and procedures.

Used by: CMS, ONC, NIH, and federal health IT programs for development and testing.

**License**: Apache 2.0 — free for any use, no registration required.

### Download

Pre-generated samples (1000+ patients, no install needed):
https://synthea.mitre.org/downloads — click the CSV button.

Generate custom data (requires Java JDK 17):
```bash
# Download JAR
wget https://github.com/synthetichealth/synthea/releases/latest/download/synthea-with-dependencies.jar

# Generate 200 Washington state patients with CSV export
java -jar synthea-with-dependencies.jar \
  -p 200 \
  --exporter.csv.export=true \
  --exporter.fhir.export=false \
  Washington "Seattle"

# Output: ./output/csv/
```

Data dictionary reference:
https://github.com/synthetichealth/synthea/wiki/CSV-File-Data-Dictionary

GitHub source:
https://github.com/synthetichealth/synthea/tree/master

### Synthea CSV Files Generated (16 total)

| File | Used In Project | Phase |
|---|---|---|
| `patients.csv` | `patients` table | Phase 1 |
| `organizations.csv` | `organizations` table | Phase 1 |
| `providers.csv` | `providers` table | Phase 1 |
| `encounters.csv` | `encounters` table | Phase 1 |
| `conditions.csv` | `conditions` table | Phase 1 |
| `allergies.csv` | `allergies` table | Phase 1 |
| `medications.csv` | `medications` table | Phase 2 (AI) |
| `observations.csv` | `observations` table | Phase 2 (AI) |
| `procedures.csv` | `procedures` table | Phase 2 (AI) |
| `careplans.csv` | Not used | — |
| `claims.csv` | Not used | — |
| `claims_transactions.csv` | Not used | — |
| `devices.csv` | Not used | — |
| `imaging_studies.csv` | Not used | — |
| `immunizations.csv` | Not used | — |
| `payer_transitions.csv` | Not used | — |
| `payers.csv` | Not used | — |
| `supplies.csv` | Not used | — |

### Key Design Decision — Synthea UUIDs as Primary Keys

Synthea generates UUIDs for every patient, encounter, provider, and organization. These UUIDs
become the primary keys in our database. This means:

- No ID translation layer needed during data load
- Foreign key relationships from Synthea CSV map directly to table relationships
- Firebase UID is a separate nullable field — added when a user registers and claims a record

---

## 4. Data Model

### Design Philosophy

Schema is derived directly from Synthea CSV column structure. We store Synthea data as close to
source as possible and add only what the application needs: Firebase UID linkage and audit trail.

No `custom_data JSONB` escape hatches. Real columns from real data.

### Entity Relationship Overview

```
organizations (Synthea: organizations.csv)
    └── providers (Synthea: providers.csv)
            └── encounters (Synthea: encounters.csv)
                    ├── conditions (Synthea: conditions.csv)
                    ├── allergies  (Synthea: allergies.csv)
                    ├── medications (Synthea: medications.csv)
                    └── observations (Synthea: observations.csv)

patients (Synthea: patients.csv)
    └── encounters (via patient_id FK)

audit_logs (application layer — not from Synthea)
```

### Table Definitions

#### `patients`
Source: `patients.csv`
Firebase UID added for auth linkage. Nullable — Synthea data loads first, users claim records on registration.

| Column        | Type                  | Notes                  |
|---------------|-----------------------|------------------------|
| id            | UUID PK               | Synthea patient UUID |
| firebase_uid  | VARCHAR(128) UNIQUE   | Null until user registers |
| birthdate     | DATE NOT NULL         | |
| deathdate     | DATE                  | Null if alive |
| prefix        | VARCHAR(10)           | Mr, Mrs, Dr etc |
| first_name    | VARCHAR(100) NOT NULL | |
| last_name     | VARCHAR(100) NOT NULL | |
| suffix        | VARCHAR(10)           | |
| maiden        | VARCHAR(100)          | |
| marital       | VARCHAR(1)            | S/M/D/W |
| race          | VARCHAR(50)           | |
| ethnicity     | VARCHAR(50)           | |
| gender        | VARCHAR(10) NOT NULL  | M/F |
| birthplace    | VARCHAR(255)          | |
| address       | VARCHAR(255)          | |
| city          | VARCHAR(100)          | |
| state         | VARCHAR(50)           | |
| zip           | VARCHAR(10)           | |
| lat           | DECIMAL(9,6)          | |
| lon           | DECIMAL(9,6)          | |
| income        | INTEGER               | |
| created_at    | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |
| updated_at    | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

#### `organizations`
Source: `organizations.csv`
Hospitals and clinics that providers work at.

| Column    | Type                  | Notes             |
|-----------|-----------------------|-------------------|
| id        | UUID PK               | Synthea org UUID |
| name      | VARCHAR(255) NOT NULL | |
| address   | VARCHAR(255)          | |
| city      | VARCHAR(100)          | |
| state     | VARCHAR(50)           | |
| zip       | VARCHAR(10)           | |
| phone     | VARCHAR(20)           | |
| lat       | DECIMAL(9,6)          | |
| lon       | DECIMAL(9,6)          | |

#### `providers`
Source: `providers.csv`
Clinicians. Firebase UID nullable — added if provider registers.

| Column            | Type                      | Notes                 |
|-------------------|---------------------------|-----------------------|
| id                | UUID PK                   | Synthea provider UUID |
| organization_id   | UUID FK → organizations   | |
| firebase_uid      | VARCHAR(128) UNIQUE       | Null until provider registers |
| name              | VARCHAR(255) NOT NULL     | |
| gender            | VARCHAR(10)               | |
| speciality        | VARCHAR(100)              | Synthea spelling: speciality |
| address           | VARCHAR(255)              | |
| city              | VARCHAR(100)              | |
| state             | VARCHAR(50)               | |
| zip               | VARCHAR(10)               | |
| lat               | DECIMAL(9,6)              | |
| lon               | DECIMAL(9,6)               | |

#### `encounters`
Source: `encounters.csv`
All clinical visits — replaces old `appointments` table. Encounter is the correct FHIR term.

| Column            | Type                          | Notes                 |
|-------------------|-------------------------------|------------------------|
| id                | UUID PK                       | Synthea encounter UUID |
| patient_id        | UUID NOT NULL FK → patients   | |
| provider_id       | UUID FK → providers           | |
| organization_id   | UUID FK → organizations       | |
| start_time        | TIMESTAMPTZ NOT NULL          | |
| stop_time         | TIMESTAMPTZ                   | |
| encounter_class   | VARCHAR(50)                   | ambulatory, emergency, inpatient, wellness, urgentcare |
| code              | VARCHAR(20)                   | SNOMED-CT code |
| description       | VARCHAR(255)                  | |
| base_cost         | DECIMAL(10,2)                 | |
| total_cost        | DECIMAL(10,2)                 | |
| reason_code       | VARCHAR(20)                   | SNOMED-CT |
| reason_desc       | VARCHAR(255)                  | |
| created_at        | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

#### `conditions`
Source: `conditions.csv`
Patient diagnoses. Normalized — not stuffed into a JSONB blob.

| Column        | Type                          | Notes           |
|---------------|-------------------------------|-----------------|
| patient_id    | UUID NOT NULL FK → patients | |
| encounter_id  | UUID NOT NULL FK → encounters | |
| start_date    | DATE NOT NULL                 | |
| stop_date     | DATE                          | Null if ongoing |
| code          | VARCHAR(20) NOT NULL          | SNOMED-CT code |
| description   | VARCHAR(255) NOT NULL         | |
| PRIMARY KEY   | (patient_id, encounter_id, code) | Composite |

#### `allergies`
Source: `allergies.csv`

| Column        | Type                          | Notes                 |
|---------------|-------------------------------|------------------------|
| patient_id    | UUID NOT NULL FK → patients   | |
| encounter_id  | UUID NOT NULL FK → encounters | |
| start_date    | DATE NOT NULL                 | |
| stop_date     | DATE                          | |
| code          | VARCHAR(20)                   | RxNorm or SNOMED-CT |
| system        | VARCHAR(20)                   | RxNorm / SNOMED-CT |
| description   | VARCHAR(255) NOT NULL         | |
| allergy_type  | VARCHAR(20)                   | allergy / intolerance |
| category      | VARCHAR(20)                   | drug, food, environment |
| severity      | VARCHAR(10)                   | MILD / MODERATE / SEVERE |
| PRIMARY KEY   | (patient_id, encounter_id, code) | Composite |

#### `medications`
Source: `medications.csv` — Phase 2 (Vertex AI)

| Column        | Type                          | Notes                 |
|---------------|-------------------------------|------------------------|
| patient_id    | UUID NOT NULL FK → patients | |
| encounter_id  | UUID NOT NULL FK → encounters | |
| start_date    | DATE NOT NULL                 | |
| stop_date     | DATE                          | Null if current |
| code          | VARCHAR(20) NOT NULL          | RxNorm code |
| description   | VARCHAR(255) NOT NULL         | |
| reason_code   | VARCHAR(20)                   | |
| reason_desc   | VARCHAR(255)                  | |
| PRIMARY KEY   | (patient_id, encounter_id, code, start_date) | Composite |

#### `observations`
Source: `observations.csv` — Phase 2 (Vertex AI)
Vitals and lab results. Value stored as VARCHAR — Synthea mixes numeric and text values.

| Column        | Type                          | Notes                 |
|---------------|-------------------------------|------------------------|
| patient_id    | UUID NOT NULL FK → patients   | |
| encounter_id  | UUID NOT NULL FK → encounters | |
| obs_date      | TIMESTAMPTZ NOT NULL          | |
| category      | VARCHAR(50)                   | vital-signs / laboratory |
| code          | VARCHAR(20) NOT NULL          | LOINC code |
| description   | VARCHAR(255) NOT NULL         | |
| value         | VARCHAR(50)                   | Numeric or text |
| units         | VARCHAR(20)                   | mg/dL, kg, etc |
| obs_type      | VARCHAR(20)                   | numeric / text |
| PRIMARY KEY   | (patient_id, encounter_id, code, obs_date) | Composite |

#### `audit_logs`
Application layer — not from Synthea. Core security requirement.

| Column        | Type                              | Notes |
|---------------|-----------------------------------|------------------------|
| id            | UUID PK DEFAULT gen_random_uuid() | |
| firebase_uid  | VARCHAR(128)                      | Who made the request |
| action        | VARCHAR(10) NOT NULL              | GET / POST / PUT / DELETE |
| resource_type | VARCHAR(50) NOT NULL              | patients, encounters, conditions etc |
| resource_id   | UUID                              | The specific record accessed |
| outcome       | VARCHAR(10) NOT NULL              | SUCCESS / FAILURE |
| source_ip     | INET                              | |
| user_agent    | TEXT                              | |
| details       | JSONB                             | Additional context        |
| created_at    | TIMESTAMPTZ NOT NULL DEFAULT NOW() | |

---

## 5. System Architecture

### High-Level

```
Internet
    │
    ▼
Cloud Armor (WAF)           ← OWASP Top 10 blocking
    │
    ▼
Cloud Load Balancer
    │
    ▼
Cloud Run — API Gateway     ← Spring Cloud Gateway (Port 8080)
    │                          Firebase JWT validation
    │                          Rate limiting
    ├──────────────────────────────────────┐
    ▼                                      ▼
Cloud Run — Patient Service     Cloud Run — Appointment Service
(Port 8002)                     (Port 8004)
    │                                      │
    └──────────────┬───────────────────────┘
                   ▼
           Cloud SQL PostgreSQL
           (Private VPC — no public IP)
                   │
           GCP Secret Manager
           (DB password, Firebase config)

Cloud Logging ← All services emit structured logs
Cloud Audit Logs ← All GCP API calls captured
Vertex AI Gemini ← Phase 2 AI analysis endpoint
```

### Services in Scope

| Service | Phase | Port | Responsibility |
|---|---|---|---|
| API Gateway | 1 | 8080 | Routing, Firebase JWT validation, rate limiting |
| Patient Service | 1 | 8002 | Patient profile, medical history read |
| Appointment Service | 1 | 8004 | Browse encounters, book/cancel |
| Provider Service | 2 | 8003 | Provider profiles, RBAC |
| AI Service | 2 | 8005 | Vertex AI Gemini patient analysis |

---

## 6. API Design (Phase 1)

### Authentication
All protected endpoints require: `Authorization: Bearer <Firebase JWT>`

Gateway validates JWT on every request before routing to downstream services.

### Patient Service Endpoints

| Method | Endpoint                     | Auth      | Description                               |
|--------|------------------------------|-----------|-------------------------------------------|
| POST   | `/api/patients/register`     | Required  | Link Firebase UID to Synthea patient record |
| GET    | `/api/patients/me`           | Required  | Get my patient profile                    |
| PUT    | `/api/patients/me`           | Required  | Update my profile                         |
| GET    | `/api/patients/me/history`   | Required  | Get my encounter history                  |
| GET    | `/health`                    | None      | Health check                              |

### Appointment Service Endpoints

| Method | Endpoint                     | Auth      | Description                       |
|--------|------------------------------|-----------|-----------------------------------|
| GET    | `/api/encounters/available`  | None      | Browse available encounter slots  |
| POST   | `/api/encounters`            | Required  | Book an encounter slot            |
| GET    | `/api/encounters/me`         | Required  | My booked encounters              |
| PUT    | `/api/encounters/{id}/cancel` | Required | Cancel an encounter               |
| GET    | `/health`                    | None      | Health check                      |

### Standard Error Response

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable message",
  "timestamp": "2026-03-23T10:30:00Z"
}
```

---

## 7. Security Architecture

### Layers (Defense in Depth)

```
Layer 1 — Network:    Cloud Armor WAF (OWASP Top 10, DDoS protection)
Layer 2 — Auth:       Firebase Auth + JWT validation at Gateway
Layer 3 — Access:     RBAC (patient role Phase 1, provider role Phase 2)
Layer 4 — Secrets:    GCP Secret Manager (no credentials in code or env vars)
Layer 5 — Data:       Cloud SQL in private VPC, TLS in transit
Layer 6 — Audit:      Cloud Audit Logs + structured audit_logs table
Layer 7 — CI/CD:      Workload Identity Federation — keyless GCP auth from GitHub Actions
Layer 8 — Scan:       OWASP ZAP security scan in CI pipeline
```

### Firebase Auth Flow

```
1. User registers / logs in via Firebase Auth (client)
2. Firebase issues JWT (RS256 signed)
3. Client sends JWT in Authorization header
4. API Gateway validates JWT signature against Firebase public keys
5. Gateway extracts uid, email, role from JWT claims
6. Gateway forwards request + user context to downstream service
7. Service performs RBAC check before processing
8. All access written to audit_logs table
```

### CI/CD Security — Workload Identity Federation (Public Repo Strategy)

This repo is public. The standard approach (storing a GCP service account JSON key in GitHub
Secrets) creates a long-lived credential that must be manually rotated and can leak in logs.

Instead we use GCP Workload Identity Federation (WIF): GitHub Actions proves its identity using
a short-lived OIDC token signed by GitHub's certificate authority. GCP verifies the token
cryptographically — no key file ever exists anywhere.

```
GitHub Actions runner
    │
    │  1. request OIDC token (proves: repo=X, branch=main, job=Y)
    ▼
GitHub OIDC Provider
    │
    │  2. return signed JWT (expires in minutes)
    ▼
GCP Security Token Service
    │  3. verify JWT against GitHub's public cert
    │  4. check: repo == yifeng2019uwb/healthcare-ai-microservices? ✓
    │  5. issue short-lived GCP access token (1 hour max)
    ▼
GitHub Actions uses token for terraform, docker push, etc.
Token expires — nothing to rotate, nothing to leak.
```

**What is safe to commit publicly:**

| Item                                   | In repo? | Safe?                                   |
|----------------------------------------|----------|-----------------------------------------|
| Workload Identity Provider resource ID | Yes      | Yes — useless without GitHub OIDC token |
| Service account email                  | Yes      | Yes — harmless alone                    |
| Terraform resource definitions         | Yes      | Yes — no real values                    |
| `terraform.tfvars.example`             | Yes      | Yes — template only                     |
| GCP credentials / key file             | No — doesn't exist | N/A                           |
| `terraform.tfvars` (real values)       | No — gitignored | N/A                              |
| DB password                            | No — GCP Secret Manager| N/A                       |

**Console log leak prevention:**

All Terraform outputs that contain sensitive values are marked `sensitive = true`.
Secrets are passed to GitHub Actions steps via `env:` blocks referencing Secret Manager,
never via `echo` or `-var=` flags. Integration tests read credentials from the environment,
never print them.

```yaml
# Correct pattern — secret injected via env, never echoed
- name: Run integration tests
  env:
    DB_PASSWORD: ${{ secrets.DB_PASSWORD }}  # masked in logs automatically
  run: ./mvnw verify -Dspring.profiles.active=integration
```

**Reference:** See `docs/CICD_SECURITY.md` for full setup guide, WIF configuration
commands, and GitHub Actions workflow files.

### GCP Secret Manager — Secrets Stored

| Secret Name                   | Content                                 |
|-------------------------------|-----------------------------------------|
| `db-password`                 | Cloud SQL PostgreSQL password           |
| `firebase-project-id`         | Firebase project identifier             |
| `firebase-service-account`    | Firebase Admin SDK credentials          |
| `jwt-secret`                  | Internal service-to-service signing key |

### STRIDE Threat Model (Summary)

| Threat                    | Mitigation                                        |
|---------------------------|---------------------------------------------------|
| Spoofing                  | Firebase JWT validation, RS256 signed tokens      |
| Tampering                 | HTTPS only, Cloud Armor, input validation         |
| Repudiation               | audit_logs table + Cloud Audit Logs               |
| Information Disclosure    | Private VPC, RBAC, patient data isolation         |
| Denial of Service         | Cloud Armor rate limiting, Cloud Run auto-scale   |
| Elevation of Privilege    | RBAC enforced at Gateway + service level          |
| Supply Chain (CI/CD)      | WIF keyless auth, no long-lived credentials in GitHub |

---

## 8. Infrastructure (GCP)

### GCP Services Used

| Service                 | Purpose             | Free Tier              |
|-------------------------|---------------------|------------------------|
| Cloud Run               | Host all services   | 2M requests/month free |
| Cloud SQL (db-f1-micro) | PostgreSQL database | Not free but ~$7/month |
| Firebase Auth           | User authentication | 10K MAU free           |
| GCP Secret Manager      | Credential storage  | 6 secrets free         |
| Cloud Armor             | WAF                 | Pay per policy         |
| Cloud Logging           | Structured logs     | 50 GB/month free       |
| Cloud Audit Logs        | GCP API audit trail | Free                   |
| Vertex AI Gemini        | AI analysis (Phase 2) | Free quota available |

### Terraform Structure (GCP Provider)

```
healthcare-infra/
├── terraform/
│   ├── gcp/
│   │   ├── main.tf          # Provider config, project
│   │   ├── variables.tf     # Project ID, region, etc
│   │   ├── cloud_sql.tf     # PostgreSQL instance
│   │   ├── cloud_run.tf     # Service deployments
│   │   ├── secret_manager.tf # Secrets
│   │   ├── vpc.tf           # Private network
│   │   ├── cloud_armor.tf   # WAF policy
│   │   └── iam.tf           # Service accounts, roles
│   └── supabase/            # Old — kept for reference
└── scripts/
    ├── generate_data.sh     # Run Synthea, generate CSVs
    └── load_data.sh         # Load CSVs into Cloud SQL
```

### Data Loading Pipeline

```
Synthea JAR
    │
    ▼ (CSV output)
./output/csv/
    │ patients.csv
    │ organizations.csv
    │ providers.csv
    │ encounters.csv
    │ conditions.csv
    └── allergies.csv
    │
    ▼ (load_data.sh)
Cloud SQL PostgreSQL
    (COPY command via psql)
```

---

## 9. Phased Delivery Plan

### Phase 1 — Foundation (Weeks 1–4)

**Goal**: Working GCP deployment with patient + encounter APIs, real Synthea data, Firebase auth.

- [ ] GCP project setup, enable APIs
- [ ] Terraform: Cloud SQL + VPC + Secret Manager
- [ ] Generate 200-patient Synthea dataset (Washington state)
- [ ] Write and run data loading scripts
- [ ] Migrate Spring Boot services to Cloud Run
- [ ] Firebase Auth integration + JWT validation at Gateway
- [ ] Patient Service: 4 endpoints live
- [ ] Appointment Service: 5 endpoints live
- [ ] `audit_logs` table wired up on every request

### Phase 2 — Security Layer (Weeks 5–6)

**Goal**: Production-grade security on top of working APIs.

- [ ] Cloud Armor WAF policy (OWASP Top 10 rules)
- [ ] All credentials moved to Secret Manager
- [ ] OWASP ZAP scan added to GitHub Actions CI/CD
- [ ] STRIDE threat model document written
- [ ] Cloud Audit Logs review + alerting

### Phase 3 — AI + Provider (Weeks 7–8)

**Goal**: Vertex AI integration + Provider service with RBAC.

- [ ] Load `medications.csv` + `observations.csv` into Cloud SQL
- [ ] Vertex AI Gemini endpoint: `POST /api/ai/analyze/{patientId}`
- [ ] Provider Service with role-based access control
- [ ] mTLS between services (stretch goal)
- [ ] Demo video recorded

---

## 10. Repository Structure

```
healthcare-ai-microservices/
├── services/
│   ├── gateway/             # Spring Cloud Gateway
│   ├── patient-service/     # Patient APIs
│   ├── appointment-service/ # Encounter/appointment APIs
│   ├── provider-service/    # Phase 2
│   ├── ai-service/          # Phase 2 — Vertex AI
│   └── shared/              # Common entities, validation
├── frontend/
│   └── patient-portal/      # React (Phase 2)
├── healthcare-infra/
│   ├── terraform/gcp/       # GCP infrastructure
│   └── scripts/
│       ├── generate_data.sh # Synthea data generation
│       └── load_data.sh     # Cloud SQL data loading
├── data/
│   └── synthea/             # Generated CSV files (gitignored)
├── docs/
│   ├── DESIGN.md            # This document
│   ├── CICD_SECURITY.md     # WIF setup + public repo security guide
│   ├── THREAT_MODEL.md      # STRIDE analysis (Phase 2)
│   └── API.md               # API reference (Phase 2)
└── .github/
    └── workflows/
        ├── ci.yml           # Build + test + OWASP ZAP (WIF auth)
        └── cd.yml           # Deploy to Cloud Run (WIF auth, main branch only)
```

---

## 11. What Changed From Previous Design

| Previous | Current | Reason |
|---|---|---|
| Railway deployment | GCP Cloud Run | GCP free tier active, better security tooling |
| Supabase PostgreSQL | Cloud SQL PostgreSQL | GCP-native, VPC-private, IAM integration |
| Supabase Auth | Firebase Auth + Identity Platform | OAuth 2.0/OIDC, GCP-native |
| AWS S3, IAM, CloudWatch | GCP equivalents | Removed deprecated AWS design |
| `user_profiles` + `patient_profiles` split | Single `patients` table | Mirrors Synthea flat structure |
| `appointments` table | `encounters` table | Correct FHIR term, maps to Synthea directly |
| `medical_records` catch-all | `conditions`, `allergies`, `medications`, `observations` | Normalized from real Synthea columns |
| `custom_data JSONB` everywhere | Real typed columns | Derived from actual Synthea CSV fields |
| Generic synthetic data | Synthea CSV (FHIR-aligned) | Real healthcare data standard |
| Python FastAPI AI service | Vertex AI Gemini API | GCP-native, no model hosting needed |
| GitHub Secret key storage | Workload Identity Federation | Keyless, no credential file exists |
| Secrets in env vars / config | GCP Secret Manager only | Nothing sensitive in repo or logs |

---

*Healthcare AI Platform — Master Design Document v2.0*
*Built with: GCP · Spring Boot · Firebase Auth · Synthea · Vertex AI*