# Provider Service Design

> Version: 2.0 | Last Updated: April 2026

---

## Overview

Manages provider profiles, organizations, and provider access to patient data.
Providers onboard new patients and view their patients' clinical data.

Owns: `providers`, `organizations` tables
Reads: `patients`, `encounters`, `conditions`, `allergies` tables

---

## Out of Scope (Phase 1)

- Provider account creation — providers pre-exist from Synthea data; they register an auth account via auth-service using their provider_code
- Organization management — use existing Synthea organization data
- Provider profile updates
- Writing conditions/allergies — Phase 2

---

## API Endpoints

### Provider self-service (PROVIDER role)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/provider/me` | Get own provider profile |
| POST | `/api/provider/patients/onboard` | Onboard new patient, generate MRN |
| GET | `/api/provider/patients` | List all my patients |
| GET | `/api/provider/patients/{id}` | View patient profile |
| GET | `/api/provider/patients/{id}/conditions` | View patient conditions |
| GET | `/api/provider/patients/{id}/allergies` | View patient allergies |
| GET | `/health` | Health check |

---

## Request / Response

### GET `/api/provider/me`

Response `200`:
```json
{
  "id": "uuid",
  "provider_code": "PRV-000001",
  "name": "Dr. John Smith",
  "gender": "M",
  "speciality": "GENERAL PRACTICE",
  "organization": {
    "id": "uuid",
    "name": "Seattle Medical Center",
    "address": "123 Main St",
    "city": "Seattle",
    "state": "WA",
    "phone": "206-555-0100"
  },
  "phone": "206-555-0200",
  "license_number": "WA-12345",
  "bio": "General practitioner with 10 years experience",
  "is_active": true
}
```

---

### POST `/api/provider/patients/onboard`

Provider creates a new patient record and generates MRN.
Provider gives MRN to patient so they can register an auth account via auth-service.

Request:
```json
{
  "first_name": "John",
  "middle_name": "A",
  "last_name": "Doe",
  "birthdate": "1990-01-15",
  "gender": "M",
  "race": "white",
  "ethnicity": "nonhispanic",
  "address": "123 Main St",
  "city": "Seattle",
  "state": "WA",
  "zip": "98101",
  "phone": "206-555-0100",
  "emergency_contact": "Jane Doe - 206-555-0101",
  "blood_type": "A+",
  "notes": "New patient"
}
```

Response `201`:
```json
{
  "id": "uuid",
  "mrn": "MRN-000224",
  "first_name": "John",
  "last_name": "Doe",
  "birthdate": "1990-01-15",
  "gender": "M",
  "created_at": "2026-03-26T10:00:00Z"
}
```

Errors:
- `400` — validation error

---

### GET `/api/provider/patients`

Returns all patients under this provider's care. Paginated.

> **Design decision — patient list scope:**
>
> Two options were considered:
>
> - **Option A**: Return only patients with at least one encounter (encounter-based). Problem: newly onboarded patients with no visits yet would be invisible.
> - **Option B**: Return all patients explicitly assigned to this provider via a `provider_patients` join table, regardless of whether any encounter exists yet.
>
> **Decision: Option B** — add a `provider_patients` join table.
> When a provider onboards a patient (`POST /onboard`), a row is inserted into `provider_patients`.
> `GET /patients` queries this table, not the encounters table.
> The encounter-based access check (`requireEncounterAccess`) remains for read access to clinical data.
>
> **TODO (implementation)**: Create `provider_patients` table in Terraform + schema. Update `registerPatient` → `onboardPatient` in `ProviderServiceImpl` to insert into this table. Update `getPatients()` to query `provider_patients` instead of deriving from encounters.

Response `200`:
```json
{
  "total": 45,
  "page": 1,
  "size": 10,
  "patients": [
    {
      "id": "uuid",
      "mrn": "MRN-000001",
      "first_name": "John",
      "last_name": "Doe",
      "birthdate": "1990-01-15",
      "gender": "M",
      "phone": "206-555-0100",
      "last_encounter_date": "2024-01-15T10:00:00Z"
    }
  ]
}
```

---

### GET `/api/provider/patients/{id}`

View full patient profile.
Provider must have at least one encounter with this patient OR have onboarded them.

Response `200`: full patient profile

Errors:
- `403` — provider has no relationship with this patient
- `404` — patient not found

---

### GET `/api/provider/patients/{id}/conditions`

Response `200`:
```json
{
  "total": 3,
  "conditions": [
    {
      "code": "44054006",
      "system": "SNOMED-CT",
      "description": "Diabetes mellitus type 2",
      "start_date": "2020-03-15",
      "stop_date": null,
      "status": "active"
    }
  ]
}
```

---

### GET `/api/provider/patients/{id}/allergies`

Response `200`:
```json
{
  "total": 2,
  "allergies": [
    {
      "code": "111088007",
      "system": "SNOMED-CT",
      "description": "Latex allergy",
      "allergy_type": "allergy",
      "category": "environment",
      "severity1": "MILD",
      "start_date": "2018-05-10",
      "stop_date": null
    }
  ]
}
```

---

## Authorization Rules

```
PROVIDER role:
  - Can only see patients they have onboarded or have encounters with
  - Cannot see other providers' patients
  - Cannot write conditions or allergies (Phase 2)

Patient access validation (two accepted paths):
  1. Patient was onboarded by this provider:
     SELECT COUNT(*) FROM provider_patients
     WHERE provider_id = :providerId AND patient_id = :patientId

  2. Provider has at least one encounter with this patient:
     SELECT COUNT(*) FROM encounters
     WHERE patient_id = :patientId AND provider_id = :providerId

  Either condition grants access. Zero results on both = 403 Forbidden.
```

---

## Audit Logging

| Event | Action | Resource Type |
|---|---|---|
| Get own profile | READ | providers |
| Onboard patient | CREATE | patients |
| List patients | READ | patients |
| View patient | READ | patients |
| View conditions | READ | conditions |
| View allergies | READ | allergies |

---

## Phase Status

- [x] Provider entity + repository
- [x] Organization entity + repository
- [x] GET /me endpoint
- [x] POST /patients/onboard endpoint (was /register — renamed)
- [x] GET /patients list endpoint
- [x] GET /patients/{id} endpoint
- [x] GET /patients/{id}/conditions endpoint
- [x] GET /patients/{id}/allergies endpoint
- [x] Audit logging integration
- [x] Unit tests
- [x] Deploy to Cloud Run
- [ ] `provider_patients` join table — schema + Terraform (Issue 4 fix)
- [ ] Update `getPatients()` to query `provider_patients` instead of encounters
- [ ] Update access check to accept onboard relationship OR encounter relationship
- [ ] POST /patients/{id}/conditions — provider write (Phase 2)
- [ ] POST /patients/{id}/allergies — provider write (Phase 2)
