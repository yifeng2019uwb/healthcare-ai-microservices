# Provider Service Design

> Version: 1.0 | Last Updated: March 2026

---

## Overview

Manages provider profiles, organizations, and provider access to patient data.
Providers can create new patient records and view their patients.

Owns: `providers`, `organizations` tables
Reads: `patients`, `encounters`, `conditions`, `allergies` tables

---

## Out of Scope (Phase 1)

- Provider self-registration — use existing Synthea provider data
- Organization management — use existing Synthea organization data
- Provider profile updates
- Writing conditions/allergies — Phase 2

---

## API Endpoints

### Provider self-service (PROVIDER role)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/provider/me` | Get own provider profile |
| POST | `/api/provider/patients/register` | Create new patient record, generate MRN |
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

### POST `/api/provider/patients/register`

Provider creates a new patient record. MRN auto-generated.
Provider gives MRN to patient for account registration.

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

Returns all patients this provider has encounters with.
Paginated.

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
Provider must have at least one encounter with this patient.

Response `200`: full patient profile

Errors:
- `403` — provider has no encounter with this patient
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
  - Can only see patients they have encounters with
  - Cannot see other providers patients
  - Cannot update patient clinical data (Phase 2)

Provider patient access validation:
  SELECT COUNT(*) FROM encounters
  WHERE patient_id = :patientId
  AND provider_id = (SELECT id FROM providers WHERE auth_id = :authId)
  → 0 results = 403 Forbidden
```

---

## Audit Logging

| Event | Action | Resource Type |
|---|---|---|
| Get own profile | READ | providers |
| Register patient | CREATE | patients |
| List patients | READ | patients |
| View patient | READ | patients |
| View conditions | READ | conditions |
| View allergies | READ | allergies |

---

## Phase Status

- [x] Provider entity + repository
- [x] Organization entity + repository
- [x] GET /me endpoint
- [x] POST /patients/register endpoint
- [x] GET /patients list endpoint
- [x] GET /patients/{id} endpoint
- [x] GET /patients/{id}/conditions endpoint
- [x] GET /patients/{id}/allergies endpoint
- [x] Provider encounter authorization check
- [x] Audit logging integration
- [x] Unit tests
- [x] Deploy to Cloud Run