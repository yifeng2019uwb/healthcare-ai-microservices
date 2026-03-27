# Appointment Service Design

Overview
Manages encounters and appointments.
Phase 1 — read-only access to Synthea encounter data.
Phase 2 — adds booking, scheduling, and availability.
Owns: encounters table
Reads: patients, providers, organizations tables

Phase 1 — Read Only (Current)
Encounter data loaded from Synthea CSV. No booking functionality yet.
Both patients and providers can view encounters via this service.

Appointment Status (Phase 2)
Status flow:
Patient sees: Booked, Completed only
Provider sees: Available, Booked, Completed
audit_logs: all actions including cancel

Phase 2 — Booking (Future)
Provider creates available appointment slots.
Patient books or cancels appointments.
Browse available slots.

---

## API Endpoints

### Phase 1 — Patient access (PATIENT role)

| Method| Endpoint                               | Description |
|-------|----------------------------------------|--------------------------------|
| GET   |  `/api/appointments/me/encounters`     | Patient views own encounters   |
| GET   | `/api/appointments/me/encounters/{id}` | Patient views encounter detail |
| GET   | `/health`                              | Health check |

### Phase 1 — Provider access (PROVIDER role)

| Method| Endpoint                                              | Description                       |
|-------|-------------------------------------------------------|-----------------------------------|
| GET   | `/api/appointments/provider/encounters`               | Provider views own encounters     |
| GET   | `/api/appointments/provider/encounters/{id}`          | Provider views encounter detail   |
| GET   | `/api/appointments/provider/patients/{id}/encounters` | Provider views patient encounters |

### Phase 2 — Booking (PROVIDER role)

| Method| Endpoint                  | Description                             |
|-------|---------------------------|----------------------------------------|
| POST  | `/api/appointments`       | Provider books appointment for patient |
| GET   | `/api/appointments/slots` | Browse available slots                  |

### Phase 2 — Patient management (PATIENT role)

| Method| Endpoint                        | Description                 |
|-------|---------------------------------|-----------------------------|
| PUT   | `/api/appointments/{id}/book`   | Patient books appointment |
| PUT   | `/api/appointments/{id}/cancel` | Patient cancels appointment |

---

## Request / Response

### GET `/api/appointments/me/encounters`

Supports filtering by date range and encounter class.

Query params:
- `from` — start date (optional)
- `to` — end date (optional)
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

### GET `/api/appointments/me/encounters/{id}`

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

---

### GET `/api/appointments/provider/encounters`

Provider views their own encounter list with time range filter.

Query params:
- `from` — start date (optional)
- `to` — end date (optional)
- `patient_id` — filter by patient (optional)
- `page` — page number (default 1)
- `size` — page size (default 10)

Response `200`: same structure as patient encounters list

---

### Phase 2 — POST `/api/appointments`

Provider books appointment for patient.

Request:
```json
{
  "patient_id": "",
  "start_time": "2026-04-01T10:00:00Z",
  "stop_time": "2026-04-01T10:30:00Z",
  "encounter_class": "ambulatory",
  "description": "Follow-up visit",
  "reason_desc": "Diabetes management"
}
```

Response `201`: created encounter

---

### Phase 2 — PUT `/api/appointments/{id}/book`

Patient books an available slot.
Response 200: updated encounter with status=Booked, patient_id linked

Errors:
404 — appointment not found
409 — appointment already booked

---

### Phase 2 — PUT `/api/appointments/{id}/cancel`

Patient cancels their appointment.
Only future appointments can be cancelled.

Response `200`: updated encounter with cancelled status

Errors:
- `403` — not patient's appointment
- `400` — appointment already passed

---

## Authorization Rules

```
PATIENT role:
  - Can only view own encounters
  - Can book an available appointments (Phase 2)
  - Can cancel own future appointments (Phase 2)

PROVIDER role:
  - Can view own encounters
  - Can view encounters of their patients
  - Can create appointments for patients (Phase 2)
  - Can book appointment on behalf of patient (Phase 2)
  - Can cancel appointment on behalf of patient (Phase 2)
```

---

## Audit Logging

| Event | Action | Resource Type |
|---|---|---|
| View encounters | READ | encounters |
| View encounter detail | READ | encounters |
| Book appointment | CREATE | encounters |
| Cancel appointment | UPDATE | encounters |

---

## Phase Status

### Phase 1
- [ ] Encounter entity + repository
- [ ] GET /me/encounters endpoint
- [ ] GET /me/encounters/{id} endpoint
- [ ] GET /provider/encounters endpoint
- [ ] GET /provider/patients/{id}/encounters endpoint
- [ ] Date range filtering
- [ ] Audit logging integration
- [ ] Unit tests
- [ ] Deploy to Cloud Run

### Phase 2
- [ ] POST /appointments endpoint (provider books)
- [ ] PUT /appointments/{id}/cancel endpoint (patient cancels)
- [ ] GET /appointments/slots endpoint
- [ ] Availability management