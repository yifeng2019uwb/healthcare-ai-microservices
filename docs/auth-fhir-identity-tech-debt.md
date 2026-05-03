# Auth ↔ FHIR Identity Validation — Tech Debt

> **Status**: Not implemented — design agreed, implementation deferred  
> **Found**: 2026-05-02  
> **Backlog**: TD-7 through TD-10

---

## Problem

Auth identity (`auth_id`) and FHIR identity (`fhir_id`) are two separate UUIDs for the same person. Today the validation that they belong together happens at the **application layer** inside each service (`findByAuthId()` DB call), not at the **auth layer**.

This means:
- Every service independently resolves `auth_id → fhir_id` with its own DB query
- No single authoritative place that certifies "this token belongs to this FHIR record"
- A request with a valid JWT but a broken or missing FHIR link reaches the service before being caught

---

## Current Flow (Problem)

```
Client → Gateway (validates JWT: auth_id, role, username)
       → Patient-service → patientDao.findByAuthId(auth_id)  ← application layer check
       → Provider-service → providerDao.findByAuthId(auth_id) ← application layer check
```

The `users` table only stores `{ id (auth_id), username, password_hash, role }`.  
The link to FHIR is stored on the **patient side**: `patients.auth_id → users.id`.  
To resolve `auth_id → fhir_id`, services must cross into the patients/providers table.

---

## Proposed Design

### 1. Add `fhir_id` to `users` table

```sql
ALTER TABLE users ADD COLUMN fhir_id UUID;
```

At registration, set both sides in the same transaction:
- `patients.auth_id = users.id`   ← already done today
- `users.fhir_id = patients.id`   ← new

One query resolves the full identity:
```sql
SELECT fhir_id FROM users WHERE id = :auth_id
```

### 2. Embed `fhir_id` in JWT at login

Auth-service reads `fhir_id` from the `users` table (no join needed) and adds it as a JWT claim:

```json
{
  "sub": "<auth_id>",
  "role": "PATIENT",
  "username": "testpatient01",
  "fhir_id": "<FHIR UUID>"
}
```

`fhir_id` is null for users without a linked FHIR record (edge case — should not occur after registration).

### 3. Gateway validates `fhir_id` for FHIR data paths

Extend `JwtAuthFilter`: for paths that require FHIR data access (`/api/patients/**`, `/api/provider/**`), check that the JWT `fhir_id` claim is present and non-null. Reject with 403 if missing.

Gateway continues to pass only `X-User-Id`, `X-User-Role`, `X-Username` — no new headers added.

### 4. Services read `fhir_id` from JWT directly

Services extract `fhir_id` from the JWT (Authorization header is already forwarded). Replace `findByAuthId()` DB lookup with direct use of the JWT-certified `fhir_id`.

---

## Validation Rule by Path

| Path pattern | Requires `fhir_id` in JWT | Who checks |
|---|---|---|
| `/api/auth/**` | No | Gateway (public or JWT-only) |
| `/api/patients/**` | Yes | Gateway (`JwtAuthFilter`) |
| `/api/provider/**` | Yes | Gateway (`JwtAuthFilter`) |
| `/actuator/health` | No | Public |

Provider's per-patient encounter-based access (`requireEncounterAccess`) stays in the provider-service — that is a business rule, not an identity rule.

---

## Files to Change

| Service | File | Change |
|---|---|---|
| DB migration | `V__add_fhir_id_to_users.sql` | Add `fhir_id UUID` column to `users` |
| auth-service | `AuthServiceImpl.java` | At registration: set `users.fhir_id`; at login: add `fhir_id` JWT claim |
| auth-service | `User.java` (entity) | Add `fhir_id` field |
| gateway | `JwtAuthFilter.java` | Validate `fhir_id` present for FHIR paths |
| gateway | `GatewayConfig.java` | Add `fhir-required-paths` config list |
| gateway | `application.yml` | Add `fhir-required-paths` entries |
| patient-service | `PatientServiceImpl.java` | Replace `findByAuthId()` with JWT `fhir_id` |
| provider-service | `ProviderServiceImpl.java` | Replace `findByAuthId()` with JWT `fhir_id` |

---

## Design Rules (do not skip)

Before implementing any part of this:
1. Design the DB migration first — confirm both columns (`patients.auth_id`, `users.fhir_id`) are set atomically in the same transaction
2. Agree on how services receive `fhir_id` — from JWT directly (requires JWT parsing in services) or via a shared utility
3. Update integration tests for the new JWT claims before changing service logic
4. Do not change services until gateway validation is deployed and verified
