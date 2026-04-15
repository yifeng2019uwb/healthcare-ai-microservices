Patient Service Design

Version: 1.0 | Last Updated: March 2026


Overview
Manages patient profiles and clinical data. Serves both patient self-service and provider access to patient data.
Owns: patients, conditions, allergies tables.
Reads: encounters, providers, organizations tables.

Responsibilities

Provider onboards new patients (creates patient record, generates MRN) — handled by provider-service
Patient views and updates own profile
Patient views own clinical history (encounters, conditions, allergies)
Provider access to patient data — handled by provider-service

Out of Scope
Provider access to patient data and patient record creation are handled by provider-service. See docs/provider-service-design.md.
---

## Database Access

| Table       | Read                     | Write                            |
|-------------|--------------------------|----------------------------------|
| patients    | patient (own) + provider | patient (own profile) + provider |
| conditions  | patient (own) + provider | provider only                    |
| allergies   | patient (own) + provider | provider only                    |
| encounters  | patient (own) + provider | read only                        |
| providers   | read                     | none                             |
| organizations | read                   | none                             |

---

## API Endpoints

### Patient self-service (PATIENT role, own data only)

| Method| Endpoint                    | Description           |
|-------|-----------------------------|-----------------------|
| GET | `/api/patients/me`            | Get full profile      |
| PUT | `/api/patients/me`            | Update own profile    |
| GET | `/api/patients/me/encounters` | Get encounter history |
| GET | `/api/patients/me/conditions` | Get conditions        |
| GET | `/api/patients/me/allergies`  | Get allergies         |

### Health check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET   | `/health` | Health check |

---

### GET `/api/patients/me`

Returns full patient profile — auth info + patient data combined.

Response `200`:
```json
{
  "id": "uuid",
  "mrn": "MRN-000001",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "PATIENT",
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
  "notes": "Initial registration",
  "created_at": "2026-03-26T10:00:00Z",
  "updated_at": "2026-03-26T10:00:00Z"
}
```

---

### PUT `/api/patients/me`

Patient updates own profile. Clinical fields (conditions, allergies)
not updatable by patient — provider only.

Request:
```json
{
  "phone": "206-555-0200",
  "address": "456 Oak Ave",
  "city": "Seattle",
  "state": "WA",
  "zip": "98102",
  "emergency_contact": "Jane Doe - 206-555-0101",
  "blood_type": "A+",
  "notes": "Updated address"
}
```

Response `200`: updated patient profile (same as GET /me)

---

### GET `/api/patients/me/encounters`

Response `200`:
```json
{
  "total": 45,
  "page": 1,
  "size": 10,
  "encounters": [
    {
      "id": "uuid",
      "start_time": "2024-01-15T10:00:00Z",
      "stop_time": "2024-01-15T10:30:00Z",
      "encounter_class": "ambulatory",
      "description": "Encounter for problem",
      "provider": {
        "id": "uuid",
        "name": "Dr. Smith",
        "speciality": "GENERAL PRACTICE"
      },
      "organization": {
        "id": "uuid",
        "name": "Seattle Medical Center",
        "city": "Seattle"
      },
      "reason_desc": "Annual checkup",
      "total_cost": 150.00
    }
  ]
}
```

---

### GET `/api/patients/me/conditions`

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
      "encounter_id": "uuid",
      "status": "active"
    }
  ]
}
```

---

### GET `/api/patients/me/allergies`

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
      "start_date": "2018-05-10",
      "stop_date": null,
      "severity1": "MILD",
      "description1": "Wheal",
      "notes": null
    }
  ]
}
```

---

### GET `/api/patients/{id}`

Provider access — returns full patient profile.
Provider must have at least one encounter with this patient.

Response `200`: same structure as GET /me

Errors:
- `403` — provider has no encounter with this patient
- `404` — patient not found

---

Provider adds condition after visit.

Request:
```json
{
  "encounter_id": "uuid",
  "code": "44054006",
  "system": "SNOMED-CT",
  "description": "Diabetes mellitus type 2",
  "start_date": "2026-03-26"
}
```

Response `201`: created condition

Errors:
- `403` — provider not authorized for this patient
- `404` — encounter not found
- `409` — condition already exists for this encounter

---

Provider adds allergy after visit.

Request:
```json
{
  "encounter_id": "uuid",
  "code": "111088007",
  "system": "SNOMED-CT",
  "description": "Latex allergy",
  "allergy_type": "allergy",
  "category": "environment",
  "start_date": "2026-03-26",
  "severity1": "MILD",
  "description1": "Wheal"
}
```

Response `201`: created allergy

---

## Authorization Rules
PATIENT role:
  - Can only access /api/patients/me/** (own data only)
  - Cannot access /api/patients/{id}/** (other patients)
  - Cannot write conditions or allergies
  - Can update own profile fields only

PROVIDER role:
  - Access to patient data handled by provider-service
  - See provider-service-design.md

---

## Audit Logging

Every access written to `audit_logs`:

| Event         | Action  | Resource Type |
|---------------|---------|---------------|
| Get profile   | READ    | patients |
| Update profile| UPDATE  | patients |
| Get encounters| READ    | encounters |
| Get conditions| READ    | conditions |
| Get allergies | READ    | allergies |

---

## Error Response Format

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable message",
  "timestamp": "2026-03-26T10:00:00Z"
}
```

---

## Phase Status

- [x] Patient entity + repository
- [x] Conditions entity + repository
- [x] Allergies entity + repository
- [x] GET /api/patients/me
- [x] PUT /api/patients/me
- [x] GET /api/patients/me/encounters
- [x] GET /api/patients/me/conditions
- [x] GET /api/patients/me/allergies
- [x] Audit logging
- [x] Deploy to Cloud Run
- [ ] GET /api/patients/{id} — provider access (Phase 2)
- [ ] POST /api/patients/{id}/conditions — provider write (Phase 2)
- [ ] POST /api/patients/{id}/allergies — provider write (Phase 2)