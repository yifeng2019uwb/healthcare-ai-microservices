# AI Service — Design Discussion

> **Status**: In progress — recommendations provided per question, but nothing is a final decision.
> Review and align before writing any code.
> Last Updated: 2026-05-15

This document captures open questions, options, tradeoffs, and recommendations.
Each question ends with a recommendation for the current scope and a note on how to scale later.

---

## Current Scope vs Future Scale

The goal is to ship a small, correct, demonstrable AI feature — then expand.
This framing applies to every design decision below.

| Dimension | Current scope              | Future scale |
|-----------|----------------------------|-----------------------------------------------|
| Language  | Java (same stack, same DB) | Add Python service if custom ML models needed |
| Trigger   | RabbitMQ: provider write → publish to `healthcare.ai` exchange → ai-service `@RabbitListener` | Add concurrency in Spring AMQP; swap to GCP Pub/Sub if moving fully to GCP |
| Input data| Conditions + allergies + encounters | + Medications + observations (load CSVs first) |
| Output    | Summarization + risk flags | + Care gaps, medication review, Blue Button claims |
| Access    | PROVIDER only              | + PATIENT-facing summaries |
| Deployment| Single Cloud Run instance  | Horizontal scale, response caching |

Design decisions should default to the current-scope column.
Scaling paths are noted so the architecture does not block them later.

---

## Open Question 1 — Language / Framework

### Option A: Java / Spring Boot
- Same language as all other services
- Uses shared module entities and DAOs directly — no second DB client, no new connection pool
- Vertex AI has a first-class Java SDK (`google-cloud-vertexai`)
- Same Dockerfile pattern, same Cloud Run setup, same deploy script
- Less associated with AI/ML on a resume

### Option B: Python / FastAPI
- Original design doc intent
- Python dominates the ML ecosystem — stronger signal for AI/ML roles on a resume
- Requires its own DB connection (SQLAlchemy + psycopg2), separate from the Java stack
- Vertex AI also has a Python SDK (`google-cloud-aiplatform`)
- Adds a second language — more surface area to maintain and deploy

### Comparison

|               | Java                                  | Python                                  |
|---------------|---------------------------------------|-----------------------------------------|
| DB access     | Shared entities, no extra config      | Separate driver + connection pool |
| Vertex AI SDK | First-class (`google-cloud-vertexai`) | First-class (`google-cloud-aiplatform`) |
| Deploy        | Identical to other services           | New Dockerfile, new Cloud Run config |
| Resume signal | Strong for backend/microservices      | Stronger for AI/ML specifically |
| Stack complexity| No change                           | Adds second language |

### Recommendation
**Java for current scope.**
The AI service needs direct DB access to read patient data. Keeping it Java eliminates
a second DB client and keeps the deployment pipeline uniform. The Vertex AI Java SDK
is production-ready and covers all needed features.

Scale path: if custom ML models (fine-tuning, embeddings, data pipelines) are added later,
introduce a Python service alongside — let each language do what it does best.

---

## Open Question 2 — How Analysis Is Triggered

### Industry patterns first

**Point-of-care CDS — HL7 CDS Hooks (dominant standard)**
The industry standard for provider-facing AI and clinical decision support.
A clinician action (opening a chart, placing an order) fires a hook → CDS service returns
structured recommendation cards in real time. Epic, Cerner, and most major EHRs implement
this. CDS Hooks is widely aligned with modern EHR interoperability and CDS patterns.
Trigger model: on-demand, synchronous, one provider action → one AI response.

**Population health / risk stratification — scheduled batch**
Used by platforms like Health Catalyst, IBM Phytel, and CMS CMMI programs.
Nightly or weekly batch scores all patients for risk tiers (readmission, chronic disease
management gaps). Pre-computed results are queried by the UI — not real-time.
Appropriate at population scale (thousands of patients), not for single-patient drill-down.

**Real-time clinical alerts — event-driven**
Used in ICU monitoring, critical lab result notification, drug interaction checking at
order entry. New lab result or order → event → AI/rules engine → alert to clinician.
Requires reliable messaging infrastructure (HL7 v2 ADT feeds, FHIR Subscriptions, or
internal Pub/Sub). High operational complexity.

### Our options mapped to industry patterns

|                       | Option A: On-Demand       | Option B: Event-Driven    | Option C: Scheduled Batch |
|-----------------------|---------------------------|---------------------------|---|
| Industry analogue | CDS Hooks (point-of-care CDS) | Real-time clinical alerts | Population health platforms |
| Infrastructure needed | None                      | Pub/Sub + result store    | Cloud Scheduler |
| Latency for end user | 2–5s per request           | Instant (pre-computed)    | Up to 24h stale |
| Build complexity      | Low                       | High                      | Low |
| Data freshness        | Always current            | Near-real-time            | Daily |
| CMS relevance         | High — aligns with CDS Hooks intent | Medium          | Medium |

### Final Design (decided 2026-05-21)

**RabbitMQ pub/sub. Provider service publishes on clinical record save. AI service is the subscriber. No polling, no DB queue table.**

#### Why asynchronous?

**1. Provider write path must stay fast**
A synchronous trigger means: `POST /api/conditions → save to DB → call Gemini → return`.
The condition save itself is <50ms. A Gemini call is 2–5s. Forcing a clinician to wait
5 seconds every time they record a diagnosis is unacceptable UX — and it couples the
latency of an external AI service to a core clinical write operation. Separating the
write path from the compute path eliminates this dependency entirely.

**2. Debounce produces more accurate analysis, not just fewer calls**
A provider entering a full visit's diagnoses might save 5 conditions in 30 seconds.
Synchronous triggers would fire 5 Gemini calls for the same patient — each seeing a
partial picture. The 5th analysis (all conditions present) is the only one that matters
clinically. The consumer-side debounce check ensures only one Gemini call fires after
all updates settle, making the result more accurate, not just cheaper.

**3. External API failures must not block clinical workflows**
Gemini is an external service — it can be down (503), rate-limited (429), or slow
(timeout). If the AI call is synchronous, every condition save fails when Gemini has
issues. With RabbitMQ, the condition saves successfully, the message waits in the queue,
and analysis completes when Gemini recovers — with no data loss and no provider-facing error.
RabbitMQ's DLQ handles persistent failures without blocking the main queue.

**4. Governance requires a single controlled entrypoint**
If AI calls are triggered inline from `ConditionService`, `AllergyService`,
`EncounterService` — governance logic (audit logging, PHI handling, rate limits,
model version pinning) must be replicated in every caller. With the RabbitMQ consumer
as the single entrypoint, those controls live in one place. Every Gemini call that
ever runs in this system goes through the ai-service listener — auditable, controllable,
and consistent.

#### Data flow

```
Provider POST /api/conditions  (or /api/allergies)
  └─ ConditionService.save()
       └─ rabbitTemplate.convertAndSend("healthcare.ai", "patient.clinical.updated", payload)
            payload: { patientId, triggerType, triggeredBy, recordId }

ai-service — @RabbitListener("ai.patient-clinical-update")
  └─ Debounce check: query ai_analysis_results for result generated < 30s ago for this patient
       └─ If fresh result exists → ack message, skip Gemini call
  └─ Fetch patient data (conditions, allergies, encounters)
  └─ Call Gemini → structured JSON response
  └─ INSERT INTO ai_analysis_results (governance fields from message payload)
  └─ ack message

  On failure (Gemini down, DB error, validation error):
  └─ nack → RabbitMQ retries (max 3, with backoff via x-death header)
  └─ After 3 failures → message routes to DLQ (ai.patient-clinical-update.dlq)
```

#### RabbitMQ topology

| Component | Name | Purpose |
|---|---|---|
| Exchange | `healthcare.ai` | Topic exchange, durable |
| Queue | `ai.patient-clinical-update` | Main consumer queue, durable, DLQ-bound |
| DLQ | `ai.patient-clinical-update.dlq` | Failed messages after 3 retries |
| Routing key | `patient.clinical.updated` | All clinical update events |

#### Message payload

```json
{
  "patientId": "uuid",
  "triggerType": "CONDITION_ADDED",
  "triggeredBy": "uuid",
  "recordId": "uuid"
}
```

#### `ai_analysis_results` schema (governance fields required — append-only)

```sql
CREATE TABLE ai_analysis_results (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id       UUID        NOT NULL REFERENCES patients(id),
    generated_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    summary          TEXT        NOT NULL,
    risk_flags       JSONB       NOT NULL,
    -- AI governance fields (immutable after insert)
    trigger_type     VARCHAR(50) NOT NULL,   -- CONDITION_ADDED | ALLERGY_ADDED | MANUAL
    triggered_by     UUID        NULL,       -- provider user ID from message payload
    model_version    VARCHAR(100) NOT NULL,  -- e.g. 'gemini-1.5-pro-002'
    input_record_ids JSONB       NOT NULL    -- UUIDs of conditions/allergies included in this call
);
```

#### Idempotency

| Layer | Mechanism | Prevents |
|---|---|---|
| Debounce check | Query `ai_analysis_results` before Gemini call — skip if result < 30s old | Duplicate Gemini calls for rapid condition updates |
| At-least-once ack | Message acked only after successful DB write | Message loss on consumer crash |
| DLQ isolation | Failed messages parked in DLQ after 3 retries | Poison messages blocking the main queue |

#### Local dev

```yaml
# docker-compose.yml
rabbitmq:
  image: rabbitmq:3-management
  ports:
    - "5672:5672"    # AMQP
    - "15672:15672"  # Management UI (guest/guest)
```

#### Spring Boot wiring (overview)

```
provider-service: spring-boot-starter-amqp → RabbitTemplate.convertAndSend(...)
ai-service:       spring-boot-starter-amqp → @RabbitListener("ai.patient-clinical-update")
```

Queue/DLQ declaration handled by ai-service `RabbitMQConfig` on startup — no manual broker setup needed.

#### Scale path

```
Current:  RabbitMQ single consumer (concurrency=1)
Next:     Increase Spring AMQP concurrency — multiple threads consume in parallel, same queue
Ultimate: Swap to GCP Pub/Sub if fully migrating to GCP — same message contract,
          same ai_analysis_results table, zero API changes
```

---

## Data Simulation Strategy (decided 2026-05-15)

The goal: establish a realistic patient environment, then simulate a provider adding clinical
data over time to observe the outbox trigger and AI analysis in action.

### Setup phase — import baseline environment

1. Import all 1000 patients, providers, organizations (no clinical records yet)
2. Bulk import **first 500 rows** of `conditions.csv` + `allergies.csv` as pre-existing history
   — these records go into the DB directly via the import pipeline, no AI trigger yet
   — represents patients who already have a history before we started tracking

### Simulation phase — feed second 500 rows via provider API

3. **Split the CSVs**: conditions.csv row 501–1000 → `conditions_sim.csv`; same for allergies
4. Read each row and POST to the provider API:
   ```
   POST /api/conditions  { patientId, code, description, ... }
   POST /api/allergies   { patientId, allergen, reaction, ... }
   ```
   This is the real business path — same as a provider updating a chart.
5. Each POST triggers an UPSERT into `ai_analysis_pending` (see Q2 outbox design)
6. Within 30s, the `@Scheduled` consumer picks it up, calls Gemini, writes to `ai_analysis_results`

### Why this approach
- Import phase uses bulk pipeline — efficient for seeding thousands of rows
- Simulation phase uses the provider API path — the same code path that runs in production
- The 30s debounce window matters here: if 10 conditions are posted for the same patient
  in a loop, only one Gemini call fires (last marked_at wins via the UPSERT)
- Avoids full clinical table scan at runtime — the outbox has at most N pending rows,
  where N is the number of patients updated since the last scheduler run

### CSV preparation script (one-time)

```bash
# Split conditions.csv: first 500 rows as history, next 500 as simulation input
head -1 conditions.csv > conditions_history.csv  # header
tail -n +2 conditions.csv | head -500 >> conditions_history.csv
head -1 conditions.csv > conditions_sim.csv
tail -n +502 conditions.csv >> conditions_sim.csv
# Same pattern for allergies.csv
```

---

## Open Question 3 — What Data Goes to Gemini

### Option A: Minimal (available now)
- Active conditions (ICD-10 codes + descriptions)
- Allergies (allergen + reaction + severity)
- Recent encounters (visit type + date)

No prerequisites — data is already loaded.

### Option B: Full (requires loading medications + observations CSV first)
- Everything in Option A
- Current medications (drug name, dosage, frequency)
- Recent observations / lab results (e.g., HbA1c, blood pressure, BMI)

Makes the analysis significantly richer and more clinically meaningful.
Loading the CSVs uses the same import pipeline already built — not a large task.

### Comparison

|                               | Minimal                           | Full |
|-------------------------------|-----------------------------------|-----------------------------------------|
| Prerequisites                 | None                              | Load medications.csv + observations.csv |
| Analysis quality              | Diagnosis history + visit cadence | Full clinical picture |
| Time to first working endpoint| Days                              | + 1–2 days for CSV load |
| Risk flag accuracy            | Limited (no labs, no meds)        | Much higher |

### Recommendation
**Start with Option A (minimal) to validate the full pipeline end-to-end.**
Get a working Gemini call returning structured output before adding more data complexity.
Load medications + observations as the immediate next step — it is a small task that
unlocks significantly better analysis.

Scale path: add `medications` and `observations` to the prompt context in the second pass.
No architectural change needed — just extend the data query and the prompt template.

**Stage 1.5 — Medication summary**: after `medications.csv` is imported, extend the Gemini
prompt with current medications and add a `medication_flags` array to the output schema.
This is the highest-value single addition — medication review is clinically significant and
directly demonstrates AI governance over PHI-sensitive drug data. No trigger or infra change needed.

---

## Open Question 4 — What the Analysis Returns

Industry patterns for LLM-assisted clinical tools:

| Use Case               | Input                               | Output | Available now? |
|------------------------|-------------------------------------|---|---|
| Clinical summarization | Conditions + encounters + allergies | Plain-English history summary | Yes |
| Risk flagging          | Conditions + encounter frequency    | Structured flags with reasons | Yes |
| Care gap identification| Conditions + age + last visits     | Overdue screenings | Partial (no observation dates) |
| Medication review      | Medications + conditions            | Interaction flags | After CSV load |
| Differential support   | Symptoms / labs                     | Possible diagnoses | Out of scope |

### Recommendation
**Clinical summarization + risk flagging for current scope.**

Both use cases fit the data available now, the architecture, and project maturity
(no model training — relying entirely on Gemini's reasoning over structured input).

**Clinical summarization:**
- Input: `conditions.description` (ICD-10), `allergies.description + reaction + severity`,
  `encounters.encounter_class + start + stop`, patient age
- Prompt: "Summarize this patient's medical history for a new treating provider. Be concise. Do not diagnose."
- Output: free-text paragraph

**Risk flagging:**
- Detectable from current data: high encounter frequency (readmission risk), chronic condition
  combinations (e.g., diabetes + hypertension), multiple severe allergies, long gaps in visit cadence
- Output: structured list — each flag must include a `reason` field (CMS-facing and federal
  systems strongly emphasize explainability and auditability over opaque scoring)

**Output schema (draft — finalize before implementation):**
```json
{
  "summary": "Patient is a 58-year-old with Type 2 diabetes and hypertension...",
  "risk_flags": [
    { "flag": "High readmission risk", "reason": "4 ER visits in the past 12 months" },
    { "flag": "Chronic condition gap", "reason": "Diabetes + hypertension, last visit 14 months ago" }
  ],
  "disclaimer": "AI-generated for informational purposes only. Not a diagnosis or treatment recommendation."
}
```

Scale path: add `care_gaps` array once observations are loaded; add `medication_flags`
once medications are loaded. Output schema is additive — no breaking changes.

### Governance (non-negotiable — must resolve before any patient data is sent)

- **HIPAA / BAA**: Vertex AI is covered under Google's HIPAA BAA but must be explicitly
  enabled for the GCP project. Verify this before sending any real patient data.
- **Disclaimer**: every response must carry the disclaimer shown above.
- **Audit logging**: every AI request must be written to `audit_logs` (existing entity)
  with `action=AI_ANALYSIS`, patient ID, requesting user, and timestamp.
- **Explainability**: risk flags must include `reason` — strongly preferred/expected for CMS-facing and federal-style systems.
- **Synthea caveat**: data is synthetic. Output looks clinically plausible but reflects
  no real patient outcomes. Note this in all demos and documentation.

---

## Open Question 5 — Blue Button 2.0

The roadmap includes:
- OAuth 2.0 flow with `sandbox.bluebutton.cms.gov`
- Pull Medicare Part A/B/D claims for a test patient
- Map claims to existing Encounter/Condition model

Most CMS-specific and impressive item for a CMS interview. Most complex item overall.

### Comparison vs core Gemini endpoint

| | Blue Button 2.0 | Core Gemini endpoint |
|---|---|---|
| CMS interview signal | Very high — hands-on with CMS flagship API | High — AI on clinical data |
| Build complexity | High (OAuth flow, external API, data mapping) | Medium (new service + Vertex AI call) |
| Dependencies | External CMS sandbox account | Vertex AI enabled on GCP project |
| Risk | External API may change, sandbox stability | Low |

### Recommendation
**Defer Blue Button 2.0 until the core Gemini endpoint is working and tested.**
Build and validate the AI service end-to-end first. Blue Button adds significant
complexity and an external dependency — it should not block the first AI feature.

Scale path: add Blue Button as a separate endpoint (`POST /api/ai/bluebutton/sync`)
that pulls claims and stores them as encounters/conditions. The AI analyze endpoint
then automatically includes that data in its next call — no changes to the analysis logic.

---

## Open Question 6 — Gateway Integration

New gateway config needed:
- Route: `/api/ai/**` → `${AI_SERVICE_URL}`
- RBAC: which roles can call the AI service?

### Option A: PROVIDER only
Providers analyze their patients. Simple access model — no patient can query their own AI summary.

### Option B: PROVIDER + PATIENT
Patients can request their own summary. Requires the AI service to validate that the
`fhirId` in the JWT matches the requested `patientId` — same ownership check pattern
used in encounter and patient endpoints.

### Comparison

| | PROVIDER only | PROVIDER + PATIENT |
|---|---|---|
| Implementation | Add one role-path entry in gateway YAML | Same + ownership check in AI service |
| Security surface | Smaller | Slightly larger (patient path param validation) |
| Demo value | Good | Better — patients can see their own summary |

### Recommendation
**PROVIDER only for current scope.**
Simplest access model, no ownership validation logic needed in the AI service on day one.

Scale path: add PATIENT access by checking `fhirId` JWT claim against the requested
`patientId` path parameter — the same pattern already designed for FHIR path validation.
No gateway changes needed for this expansion, only AI service logic.

---

## Summary of Recommendations

| Question | Recommendation | Scale path |
|---|---|---|
| Language | Java / Spring Boot | Add Python service if custom ML needed |
| Trigger | RabbitMQ pub/sub + `@RabbitListener` (consumer-side 30s debounce check) | Increase AMQP concurrency; swap to GCP Pub/Sub if fully on GCP |
| Input data | Minimal (conditions, allergies, encounters) | + Medications + observations (next pass) |
| Output | Summarization + risk flags | + Care gaps, medication flags |
| Blue Button | Defer | Add after core endpoint is stable |
| Gateway | PROVIDER only | Add PATIENT role with fhirId ownership check |

---

## Implementation Checklist

- [ ] **HIPAA BAA** — In GCP Console, verify Vertex AI is listed under BAA-covered services for the project before sending any patient data.
- [ ] **Prompt engineering** — Use a Gemini `systemInstruction` to strictly enforce the "Do not diagnose" rule at the model level, not just in the output disclaimer.
- [ ] **eBPF baseline** — After deploying to Oracle VM, run `opensnoop` via the eBPF agent to verify the AI service is only touching expected DB drivers, cert files, and Vertex AI endpoints — nothing else.

---

## Notes from Discussion

- 2026-05-14: If the service reads the same DB → prefer Java (no second DB client)
- 2026-05-14: Start from a small, demonstrable case — validate pipeline before adding data complexity
- 2026-05-14: Medications/observations load is a prerequisite for richer analysis but not for a first pass
- 2026-05-14: FHIR R4 format is orthogonal to AI — not a concern for the AI service
- 2026-05-15: Narrow current scope, design for scale — each question now has a recommendation and a scale path
- 2026-05-15: Risk flags must include `reason` field — strongly preferred/expected for CMS-facing and federal-style systems; not a hard legal mandate
- 2026-05-15: HIPAA BAA for Vertex AI must be verified before sending any patient data
- 2026-05-15: Q2 architecture — separate generate/store from serve; ai_analysis_results table built now so trigger (on-demand vs Pub/Sub) is an internal detail; API contract never changes
- 2026-05-15: Trigger mechanism finalized — transactional outbox (ai_analysis_pending) + Spring @Scheduled consumer; no HTTP endpoint for AI invocation; provider POST condition/allergy is the real trigger
- 2026-05-15: Debounce window 30s — rapid condition/allergy updates per patient batch into one Gemini call (UPSERT on patient_id PRIMARY KEY ensures last marked_at wins)
- 2026-05-15: Three-layer idempotency: (1) UPSERT PRIMARY KEY dedup on write, (2) atomic single-row UPDATE processing lock with 5min expiry, (3) freshness check before Gemini call
- 2026-05-15: Crash recovery — on scheduler startup, release stale PROCESSING locks (lock_expires_at < NOW()) back to PENDING
- 2026-05-15: ai_analysis_results is append-only and immutable after insert; governance fields required: trigger_type, triggered_by, model_version, input_record_ids
- 2026-05-15: Simulation strategy — 1000 Synthea patients; conditions.csv + allergies.csv split 500/500; first 500 bulk imported as baseline history; second 500 fed row-by-row via provider POST API to trigger outbox → AI
- 2026-05-15: Scale path confirmed — outbox → Pub/Sub publisher + AI subscriber → same table; ultimate scale via Debezium CDC → Kafka; zero API changes at any step
- 2026-05-21: Trigger mechanism revised — dropped transactional outbox (ai_analysis_jobs table + @Scheduled polling) in favor of RabbitMQ pub/sub; ai_analysis_results table unchanged; CloudAMQP free tier for cloud, rabbitmq:3-management Docker image for local dev; debounce moved to consumer-side check on ai_analysis_results; DLQ replaces retry_count logic
- 2026-05-15: CDS Hooks wording softened — "widely aligned with modern EHR interoperability patterns" not "CMS mandates"; CMS explainability expectation is strong preference not a named legal requirement
- 2026-05-15: triggered_by added to ai_analysis_pending at insert time (provider ID from JWT); last writer wins on rapid updates; copied to ai_analysis_results at processing time
- 2026-05-15: ai_analysis_pending extended with COMPLETED/FAILED states, completed_at, last_error, retry_count — COMPLETED rows purged after 24h, FAILED after 7d; failure inspection and retry without re-triggering provider flow
- 2026-05-15: Stage 1.5 defined — medication summary after medications.csv import; highest-value single addition; no trigger or infra change needed
