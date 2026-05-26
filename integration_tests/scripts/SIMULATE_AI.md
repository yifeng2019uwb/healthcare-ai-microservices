# AI Analysis Simulation — Design & Steps

Simulates realistic clinical data ingestion and on-demand AI analysis requests.
Runs against a live stack via the gateway at `http://localhost:8080` (or `$GATEWAY_URL`).

---

## API Surface (final)

| Endpoint | Role | Purpose |
|---|---|---|
| `POST /api/provider/encounters/{encounterId}/conditions` | PROVIDER | Provider documents a condition on an encounter |
| `POST /api/provider/encounters/{encounterId}/allergies` | PROVIDER | Provider documents an allergy on an encounter |
| `POST /api/ai/encounters/{encounterId}/request` | PROVIDER / ADMIN | Request AI analysis on demand — sync 200 + full result |
| `GET /api/ai/patient/{patientId}` | PROVIDER / ADMIN | Get latest AI result |
| `GET /api/ai/patient/{patientId}/history` | PROVIDER / ADMIN | Get all AI results |

Conditions and allergies are provider write actions scoped under `/api/provider/` — they do not modify the encounter itself and do not collide with the appointment service's `/api/encounters/` path.
The AI trigger is completely independent of condition/allergy writes; provider calls it whenever they want analysis.

---

## Data Preparation

### Source files (already prepared by user)

```
integration_tests/test-data/SortedCVS/
  conditions.csv       ← simulation queue: 1,485 rows, START 2021–2026, sorted ascending
  conditions-old.csv   ← baseline: 2,430 rows, START 1956–2020, sorted ascending
  allergies.csv        ← simulation queue: 22 rows, START 2021–2022, sorted ascending
  allergies-old.csv    ← baseline: 91 rows, START 1983–2020, sorted ascending
```

Original source files in `test-data/csv/` are never modified.

### Synthea dataset target

Re-run Synthea for ~300 patients to get sufficient providers and patients:
- ~300 providers, ~300 patients, ~10,000 conditions, ~400 allergies
- Re-run: `./healthcare-infra/synthea/run-synthea.sh test-data 300`
- Regenerate SortedCVS files after re-run using the same date split (2021 cutoff)

### Test provider accounts

Register ~200 providers directly in DB (not via API) with a shared known password.
Save as `integration_tests/test-data/test-accounts/providers.json`:

```json
[
  { "fhirId": "34bd274a-...", "username": "provider_001", "password": "Password1@" },
  { "fhirId": "582aec23-...", "username": "provider_002", "password": "Password1@" },
  ...
]
```

- `fhirId` = provider UUID from `providers.csv` (col `Id`)
- Username format: `provider_NNN` (sequential)
- All use the same password for simplicity
- `fhirId` links the auth account to the encounter's `PROVIDER` column, so ownership checks pass

---

## Phase 1 — Baseline Import (AdminImportIT, run once)

**Goal:** Populate DB with all reference data + historical conditions/allergies (pre-2021).

**Steps (in FK order):**
1. `POST /api/admin/import/organizations` — full CSV (admin token)
2. `POST /api/admin/import/patients` — full CSV
3. `POST /api/admin/import/providers` — full CSV
4. `POST /api/admin/import/encounters` — full CSV
5. `POST /api/admin/import/conditions` — `conditions-old.csv` (2,430 rows)
6. `POST /api/admin/import/allergies` — `allergies-old.csv` (91 rows)

All calls verify HTTP 200. Import is idempotent — safe to re-run.

---

## Phase 2 — Batched Simulation (AiSimulateIT)

**Goal:** Simulate providers incrementally writing new conditions and allergies (2021+).
Each provider uses their own JWT — same code path as a real provider.

**Setup:**
- Load `conditions.csv` (1,485 rows) into memory as a list
- Load `allergies.csv` (22 rows) into memory as a list
- Load `providers.json` test accounts → login each → cache JWT per `fhirId`
- Initialize `aiTriggerList: List<{patientId, encounterId, providerJwt}>`

**Per batch (5 condition rows at a time, offset tracked in memory):**
```
1. Take next 5 rows from conditions list

2. For each condition row:
     encounterId = row[ENCOUNTER]
     patientId   = row[PATIENT]
     Look up PROVIDER for this encounter from encounters.csv → providerFhirId
     Get JWT for providerFhirId from cached logins
     If no test account for this provider → skip row (provider not registered)

     POST /api/provider/encounters/{encounterId}/conditions  (provider JWT)
     Body: { code, description, start_date, stop_date }
     Assert: 201

3. Collect patients from successful writes
   For each patient: scan allergies list for matching PATIENT
   For each matching allergy row:
     Look up providerFhirId from allergy's ENCOUNTER
     POST /api/provider/encounters/{encounterId}/allergies  (provider JWT)
     Body: { code, description, start_date, stop_date, ... }
     Assert: 201

4. Random AI flag (~20% per batch):
   Pick 1 condition row from the batch
   Add (patientId, encounterId, providerJwt) → aiTriggerList
```

**Notes:**
- Rows where the encounter's provider has no test account are skipped silently
- Allergy import is idempotent — duplicate rows across batches are safely skipped
- No temp CSV files needed — direct JSON POST per row

---

## Phase 3 — AI Trigger & Verify

**Goal:** Provider requests AI analysis on demand and verifies the result.

**Steps:**
```
For each (patientId, encounterId, providerJwt) in aiTriggerList:

  POST /api/ai/encounters/{encounterId}/request  (provider JWT)
  Assert: 200
  Assert response:
    - summary       non-null, non-empty string
    - risk_flags    valid JSON array (may be empty)
    - trigger_type  = "MANUAL"
    - model_version non-null
    - patient_id    = expected patientId
```

No separate GET needed — the trigger returns the full result synchronously.

**What counts as success:**
- All condition/allergy writes return 201
- All AI trigger calls return 200 with correct response shape
- No assertion on summary content (AI output is non-deterministic)

---

## File Layout

```
integration_tests/
  ai/
    AiSimulateIT.java                    ← new: Phase 2 + Phase 3
  admin/
    AdminImportIT.java                   ← modified: add conditions-old + allergies-old imports
  scripts/
    SIMULATE_AI.md                       ← this file
  test-data/
    SortedCVS/
      conditions.csv                     ← simulation queue (2021+, sorted)
      conditions-old.csv                 ← baseline (pre-2021, sorted)
      allergies.csv                      ← simulation queue (2021+, sorted)
      allergies-old.csv                  ← baseline (pre-2021, sorted)
    test-accounts/
      providers.json                     ← 200 test provider credentials
  util/
    ApiPaths.java                        ← add new paths
  run-it.sh                              ← add 'simulate' case
```

---

## Service Dependencies

### ✅ 1. provider-service: condition write endpoint
```
POST /api/provider/encounters/{encounterId}/conditions
Header: X-User-Id: {providerId}
Body:   { "code": "E11", "description": "...", "start_date": "2021-01-15", "stop_date": null }
Logic:  verify encounter.getProviderId() == providerId (403 if mismatch)
        derive patientId from encounter
        save Condition(patientId, encounterId, code, description, startDate, stopDate)
Return: 201 + ConditionResponse
```

### ✅ 2. provider-service: allergy write endpoint
```
POST /api/provider/encounters/{encounterId}/allergies
Header: X-User-Id: {providerId}
Body:   { "code": "...", "description": "...", "start_date": "...", ... }
Logic:  same ownership check as conditions
Return: 201 + AllergyResponse
```

### ✅ 3. ai-service: on-demand analysis endpoint
```
POST /api/ai/encounters/{encounterId}/request
Header: X-User-Id: {providerId}, X-User-Role: {role}
Logic:  if ADMIN → skip ownership check, use encounter.getProviderId() as effective provider
        if PROVIDER → verify encounter.getProviderId() == providerId (403 if mismatch)
        call runAnalysis(patientId, encounterId, MANUAL, null) synchronously
Return: 200 + AiAnalysisResponse (full result, not 202)
```

### ✅ 4. ai-service: remove windowed queue
- Deleted `ConcurrentHashMap<UUID, PendingTrigger> pendingPatients`
- Deleted `@Scheduled drainPendingPatients()`
- Deleted `queuePatientEvent()` from interface and impl
- Deleted `windowMinutes` `@Value` field
- Deleted `PendingTrigger` DTO

### ✅ 5. ai-service: remove Kafka listener
- Deleted `HealthcareEventListener.java`
- Removed Kafka consumer config from `application.yml`

### 6. gateway: add PROVIDER role for new write paths
```yaml
role-paths:
  "[/api/provider/]": PROVIDER    # all provider endpoints (conditions, allergies, read)
  "[/api/ai/]":       PROVIDER    # AI trigger + read
```

### 7. ApiPaths additions
```java
public static final String PROVIDER_ENCOUNTER_CONDITIONS = "/api/provider/encounters/{encounterId}/conditions";
public static final String PROVIDER_ENCOUNTER_ALLERGIES  = "/api/provider/encounters/{encounterId}/allergies";
public static final String AI_REQUEST                    = "/api/ai/encounters/{encounterId}/request";
public static final String AI_LATEST_RESULT              = "/api/ai/patient/{patientId}";
public static final String AI_HISTORY                    = "/api/ai/patient/{patientId}/history";
```
