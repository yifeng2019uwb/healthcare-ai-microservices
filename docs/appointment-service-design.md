# Appointment Service Design

> Version: 2.0 | Last Updated: April 2026

---

## Overview

Manages encounter history and appointment scheduling.
Single service, two path prefixes following FHIR R4 / Epic conventions:

- `/api/encounters/**` — clinical encounter history (Phase 1, read-only Synthea data)
- `/api/appointments/**` — appointment scheduling and booking (Phase 2)

Owns: `encounters` table
Reads: `patients`, `providers`, `organizations` tables

---

## Why Two Prefixes

Following FHIR R4 and Epic conventions:

| Concept | Meaning | Path prefix |
|---|---|---|
| Encounter | Clinical event that happened or is in progress | `/api/encounters/**` |
| Appointment | Scheduled booking — patient will see provider | `/api/appointments/**` |

An appointment, once fulfilled, becomes an encounter. They are related but distinct resources.
Both prefixes route to the same `appointment-service` at the gateway.

---

## Controllers

| Controller | Path prefix | Phase | Role |
|---|---|---|---|
| `PatientEncounterController` | `/api/encounters/me` | Phase 1 | PATIENT reads own encounter history |
| `ProviderEncounterController` | `/api/encounters/provider` | Phase 1 | PROVIDER reads encounters |
| `AppointmentController` | `/api/appointments` | Phase 2 | Booking and scheduling |

---

## API Endpoints

### Phase 1 — Patient encounter history (PATIENT role)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/encounters/me` | Patient views own encounter list |
| GET | `/api/encounters/me/{id}` | Patient views encounter detail |

### Phase 1 — Provider encounter history (PROVIDER role)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/encounters/provider` | Provider views own encounter list |
| GET | `/api/encounters/provider/{id}` | Provider views encounter detail |
| GET | `/api/encounters/provider/patients/{patientId}` | Provider views all encounters for a specific patient |

### Phase 2 — Appointment booking (PROVIDER role)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/appointments` | Provider creates appointment slot for patient |
| GET | `/api/appointments/slots` | Browse available slots |

### Phase 2 — Appointment booking (PATIENT role)

| Method | Endpoint | Description |
|---|---|---|
| PUT | `/api/appointments/{id}/book` | Patient books an available slot |
| PUT | `/api/appointments/{id}/cancel` | Patient cancels their appointment |

> Provider can also book/cancel on behalf of a patient using the same endpoints.

---

## Request / Response

### GET `/api/encounters/me`

Query params:
- `from` — start date (optional, ISO 8601 date)
- `to` — end date (optional, ISO 8601 date)
- `class` — encounter class filter (optional): ambulatory, emergency, wellness
- `page` — page number (default 1)
- `size` — page size (default 10)

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
      "code": "185347001",
      "description": "Encounter for problem",
      "reason_desc": "Annual checkup",
      "base_cost": 80.63,
      "total_cost": 80.63,
      "provider": {
        "id": "uuid",
        "name": "Dr. Smith",
        "speciality": "GENERAL PRACTICE"
      },
      "organization": {
        "id": "uuid",
        "name": "Seattle Medical Center",
        "city": "Seattle"
      }
    }
  ]
}
```

---

### GET `/api/encounters/me/{id}`

Response `200`:
```json
{
  "id": "uuid",
  "start_time": "2024-01-15T10:00:00Z",
  "stop_time": "2024-01-15T10:30:00Z",
  "encounter_class": "ambulatory",
  "code": "185347001",
  "description": "Encounter for problem",
  "reason_code": "3718001",
  "reason_desc": "Annual checkup",
  "base_cost": 80.63,
  "total_cost": 80.63,
  "payer_coverage": 0.00,
  "provider": {
    "id": "uuid",
    "name": "Dr. Smith",
    "speciality": "GENERAL PRACTICE",
    "phone": "206-555-0200"
  },
  "organization": {
    "id": "uuid",
    "name": "Seattle Medical Center",
    "address": "123 Main St",
    "city": "Seattle",
    "state": "WA",
    "phone": "206-555-0100"
  }
}
```

Errors:
- `403` — encounter does not belong to this patient
- `404` — encounter not found

---

### GET `/api/encounters/provider`

Query params:
- `from` — start date (optional)
- `to` — end date (optional)
- `patient_id` — filter by patient UUID (optional)
- `page` — page number (default 1)
- `size` — page size (default 10)

Response `200`: same structure as patient encounter list

---

### GET `/api/encounters/provider/{id}`

Response `200`: same structure as patient encounter detail

Errors:
- `403` — encounter does not belong to this provider
- `404` — encounter not found

---

### GET `/api/encounters/provider/patients/{patientId}`

Returns all encounters between this provider and the specified patient.

Errors:
- `403` — provider has no encounters with this patient
- `404` — patient not found

---

### Phase 2 — POST `/api/appointments`

Provider creates an appointment slot for a patient.

Request:
```json
{
  "patient_id": "uuid",
  "start_time": "2026-04-01T10:00:00Z",
  "stop_time": "2026-04-01T10:30:00Z",
  "encounter_class": "ambulatory",
  "description": "Follow-up visit",
  "reason_desc": "Diabetes management"
}
```

Response `201`: created appointment

---

### Phase 2 — PUT `/api/appointments/{id}/book`

Patient books an available slot.

Response `200`: updated appointment with `status=Booked`

Errors:
- `404` — appointment not found
- `409` — appointment already booked

---

### Phase 2 — PUT `/api/appointments/{id}/cancel`

Patient or provider cancels a future appointment.

Response `200`: updated appointment with `status=Cancelled`

Errors:
- `403` — not patient's appointment (patient cancelling another patient's slot)
- `400` — appointment date already passed

---

## Appointment Status Flow (Phase 2)

```
Provider creates slot → AVAILABLE
Patient or provider books → BOOKED
Visit occurs → COMPLETED
Patient or provider cancels → CANCELLED  (logged in audit_log)
```

Patient sees: BOOKED, COMPLETED
Provider sees: AVAILABLE, BOOKED, COMPLETED, CANCELLED

---

## Authorization Rules

```
PATIENT role:
  - /api/encounters/me/**      — own encounters only, enforced by auth_id lookup
  - /api/appointments/{id}/book   — Phase 2
  - /api/appointments/{id}/cancel — Phase 2, own appointments only

PROVIDER role:
  - /api/encounters/provider/**   — own encounters + own patients' encounters
  - /api/appointments             — Phase 2, create slots
  - /api/appointments/{id}/book   — Phase 2, book on behalf of patient
  - /api/appointments/{id}/cancel — Phase 2, cancel on behalf of patient
```

> NOTE: Path-level role enforcement (PATIENT vs PROVIDER) is handled at the gateway.
> See gateway-service-design.md — RBAC section (planned).

---

## Audit Logging

| Event | Action | Resource Type |
|---|---|---|
| View encounter list | READ | encounters |
| View encounter detail | READ | encounters |
| Create appointment slot | CREATE | appointments |
| Book appointment | UPDATE | appointments |
| Cancel appointment | UPDATE | appointments |

---

## Phase Status

### Phase 1 — Encounter history ✅
- [x] Encounter entity + repository
- [x] `PatientEncounterController` — GET /me, GET /me/{id}
- [x] `ProviderEncounterController` — GET /provider, GET /provider/{id}, GET /provider/patients/{id}
- [x] Date range and patient_id filtering
- [x] Audit logging
- [x] Unit tests
- [ ] Deploy to Cloud Run

> TODO (code): rename PatientAppointmentController → PatientEncounterController, update path from /api/appointments/me → /api/encounters/me
> TODO (code): rename ProviderAppointmentController → ProviderEncounterController, update path from /api/appointments/provider → /api/encounters/provider
> TODO (gateway): add /api/encounters/** route to application.yml and gateway-service-design.md route table

### Phase 2 — Appointment booking ⏳
- [ ] Appointment slot data model (new table or status column on encounters)
- [ ] `AppointmentController` — POST /appointments, GET /slots, PUT /{id}/book, PUT /{id}/cancel
- [ ] Availability management
- [ ] Status flow enforcement (AVAILABLE → BOOKED → COMPLETED / CANCELLED)
- [ ] Unit tests
- [ ] Deploy to Cloud Run
