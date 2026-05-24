# AI Service — Design Discussion

> **Status**: FINAL — all decisions locked as of 2026-05-23
> Last Updated: 2026-05-23

---

## Final Decisions Summary

| Dimension | Decision | Rationale |
|-----------|----------|-----------|
| Language | Java / Spring Boot | Same stack, shared DAOs, no second DB client |
| Trigger | Debezium Server → Redpanda (Kafka) | CDC at DB level — provider service has zero coupling |
| LLM | Gemini API key (Google AI Studio) | API key only, works from any cloud, no GCP SA credentials needed |
| Input data | Conditions + allergies + encounters | Available now, no prerequisites |
| Output | Summary + risk_flags (each flag must have `reason`) | Auditable, explainable |
| Gateway | PROVIDER only, `/api/ai/**` | Simplest access model |
| Deployment | Oracle OCI VM (Docker Compose) | Free tier, different env from GCP for eBPF monitoring |
| Infrastructure as Code | Pulumi (Go) — `pulumi-oracle/` + `pulumi-supabase/` | All infra tracked in git |

---

## Current Scope vs Future Scale

| Dimension | Current scope | Future scale |
|-----------|---------------|--------------|
| Language | Java | Add Python service if custom ML models needed |
| Trigger | Debezium → Redpanda (local), Debezium → Upstash Kafka (cloud) | Increase Kafka partitions + consumer concurrency |
| Input data | Conditions + allergies + encounters | + Medications + observations (load CSVs first) |
| Output | Summary + risk flags | + Care gaps, medication flags |
| LLM | Gemini API key | Swap model version without code changes |
| Access | PROVIDER only | + PATIENT with fhirId ownership check |
| Deployment | Single Oracle OCI VM, Docker Compose | Multi-VM, container orchestration |

---

## Architecture

### Local Development

```
docker-compose.yml
├── gateway          (port 8080)
├── auth-service     (port 8082)
├── patient-service  (port 8081)
├── provider-service (port 8083)
├── ai-service       (port 8084)
├── redpanda         (port 9092)  ← Kafka-compatible, no Zookeeper
└── debezium-server               ← watches Supabase WAL → publishes to Redpanda
```

### Data Flow

```
Provider POST /api/conditions  (or /api/allergies)
  └─ ConditionService.save() → Supabase PostgreSQL
                                      ↓ WAL / logical replication
                               Debezium Server
                                      ↓
                       Redpanda topic: healthcare.conditions
                                       healthcare.allergies
                                      ↓
                       ai-service @KafkaListener
                         └─ debounce: check ai_analysis_results < 30s ago
                         └─ fetch conditions + allergies + encounters
                         └─ Gemini API call (structured JSON)
                         └─ INSERT INTO ai_analysis_results
```

### Why Debezium + Kafka (not application-level publish)

**Provider service has zero coupling to the messaging layer.**
No `rabbitTemplate.convertAndSend()` or event publisher in any service method.
Debezium watches the PostgreSQL WAL directly — any INSERT/UPDATE to `conditions`
or `allergies` triggers a Kafka event automatically.

Benefits at scale:
- At-least-once delivery guaranteed by WAL replication (not dual-write)
- Any future consumer (audit, analytics, notifications) taps the same topics without touching provider-service
- Provider write path stays fast — no external calls in the hot path
- Gemini failures never affect clinical writes

### Kafka Topology

| Component | Name | Purpose |
|-----------|------|---------|
| Broker | Redpanda (local) / Upstash Kafka (cloud) | Kafka-compatible |
| Topic | `healthcare.conditions` | Debezium CDC events for conditions table |
| Topic | `healthcare.allergies` | Debezium CDC events for allergies table |
| Consumer group | `ai-service` | ai-service Kafka consumer |

Redpanda auto-creates topics in dev mode — no manual setup needed locally.

### Debezium Server Configuration

Connects to Supabase via logical replication (PostgreSQL WAL).

**Supabase setup (managed by `pulumi-supabase/`):**
```sql
-- Publication: tells Postgres which tables to replicate
CREATE PUBLICATION debezium_pub FOR TABLE conditions, allergies;
-- Replication slot: Debezium's cursor into the WAL
SELECT pg_create_logical_replication_slot('debezium_slot', 'pgoutput');
```

Config file `docker/debezium/application.properties` (tracked in git):
```properties
debezium.source.connector.class=io.debezium.connector.postgresql.PostgresConnector
debezium.source.database.hostname=db.<ref>.supabase.co
debezium.source.database.port=5432
debezium.source.database.user=postgres
debezium.source.database.password=${SUPABASE_PASSWORD}
debezium.source.database.dbname=postgres
debezium.source.plugin.name=pgoutput
debezium.source.publication.name=debezium_pub
debezium.source.slot.name=debezium_slot
debezium.source.table.include.list=public.conditions,public.allergies
debezium.sink.type=kafka
debezium.sink.kafka.producer.bootstrap.servers=redpanda:9092
```

### Idempotency

| Layer | Mechanism | Prevents |
|-------|-----------|---------|
| Debounce check | Query `ai_analysis_results` before Gemini call — skip if result < 30s old for this patient | Duplicate Gemini calls for rapid condition updates |
| At-least-once | Kafka consumer acks only after successful DB write | Message loss on consumer crash |
| Kafka offset | Consumer group offset tracking | Reprocessing on restart |

---

## LLM: Gemini API Key

**Provider**: Google AI Studio (`generativelanguage.googleapis.com`)
**Authentication**: API key only — no GCP project, no SA credentials
**Model**: `gemini-1.5-pro` (or latest stable)
**Java integration**: REST call via Spring's `RestClient` — no SDK needed

```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent?key={API_KEY}
```

Configured via environment variable `GEMINI_API_KEY` — injected by Docker Compose.

---

## Output Schema

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

`reason` field is required on every risk flag — required for auditability and explainability.

---

## Database Schema

`ai_analysis_results.sql` already exists in `healthcare-infra/schema/sql/`.
Table is append-only and immutable after insert. Governance fields required.

Key columns: `patient_id`, `summary`, `risk_flags` (JSONB), `trigger_type`,
`triggered_by` (provider user_id), `model_version`, `input_record_ids` (JSONB).

---

## Infrastructure as Code

All infrastructure tracked in git via Pulumi (Go). No manual console commands.

```
healthcare-infra/
├── pulumi-oracle/        ← Oracle OCI VM + VCN + networking (exists)
│   ├── main.go
│   ├── network.go        ← VCN, subnet, internet gateway, security list
│   └── compute.go        ← 2x E2.1.Micro, Docker install, cloud-init
└── pulumi-supabase/      ← to be created
    └── main.go           ← publication + replication slot (pulumi/postgresql provider)
```

**`pulumi-oracle/`** provisions:
- VCN + public subnet + internet gateway + route table
- Security list: SSH (22), gateway (8080), internal VCN traffic
- Instance 1: gateway + auth-service
- Instance 2: provider-service + ai-service + Debezium Server
- Public IP assigned automatically (`AssignPublicIp: true`)
- Docker + docker-compose-plugin installed via cloud-init

**`pulumi-supabase/`** (to create) manages:
- `postgresql.Publication` → `debezium_pub` on conditions + allergies
- `postgresql.ReplicationSlot` → `debezium_slot`

---

## Gateway Integration

```yaml
# gateway application.yml additions
role-paths:
  "[/api/ai/]": PROVIDER    # add to existing role-paths map

routes:
  - id: ai-service
    uri: ${AI_SERVICE_URL}
    predicates:
      - Path=/api/ai/**
```

---

## Data Input (Phase 1)

Available now — no prerequisites:
- `conditions` — ICD-10 codes + descriptions + dates
- `allergies` — allergen + reaction + severity
- `encounters` — visit type + start/stop dates

Stage 1.5 (after medications.csv import): add `medications` to prompt context.
No trigger or infra change needed — just extend the data query and prompt template.

---

## Governance (non-negotiable)

- **Disclaimer**: every response carries the disclaimer shown in output schema above
- **Audit logging**: every AI request written to `audit_logs` (`action=AI_ANALYSIS`, patient_id, provider user_id, timestamp)
- **Explainability**: risk flags must include `reason` field
- **Synthea caveat**: data is synthetic — note this in all demos

Note: Gemini API key (Google AI Studio) does not have a HIPAA BAA — acceptable for
synthetic Synthea data. Switch to Vertex AI (with GCP BAA) before using real patient data.

---

## Data Simulation Strategy

Goal: establish a realistic patient environment, then simulate a provider adding
clinical data over time to trigger Debezium → Kafka → AI pipeline.

### Setup phase
1. Import all patients, providers, organizations (no clinical records yet)
2. Bulk import **first 500 rows** of `conditions.csv` + `allergies.csv` via admin import pipeline
   — these go directly to DB, Debezium captures them but ai-service debounce suppresses rapid-fire calls

### Simulation phase
3. Split CSVs: rows 501–1000 → `conditions_sim.csv`, `allergies_sim.csv`
4. POST each row via provider API:
   ```
   POST /api/conditions  { patientId, code, description, ... }
   POST /api/allergies   { patientId, allergen, reaction, ... }
   ```
5. Each POST → Supabase → Debezium → Redpanda → ai-service → Gemini → `ai_analysis_results`

```bash
# Split conditions.csv
head -1 conditions.csv > conditions_history.csv
tail -n +2 conditions.csv | head -500 >> conditions_history.csv
head -1 conditions.csv > conditions_sim.csv
tail -n +502 conditions.csv >> conditions_sim.csv
# Same for allergies.csv
```

---

## Implementation Checklist

- [ ] `pulumi-supabase/` — create Go Pulumi stack with `postgresql.Publication` + `postgresql.ReplicationSlot`
- [ ] `docker/debezium/application.properties` — Debezium Server config file
- [ ] `docker-compose.yml` — add Redpanda + Debezium Server containers
- [ ] `services/ai-service/` — new Spring Boot module with Kafka consumer + Gemini REST call
- [ ] `healthcare-infra/schema/sql/ai_analysis_results.sql` — already exists, deploy via `run-schema.sh`
- [ ] Gateway `application.yml` — add `/api/ai/` PROVIDER role-path + ai-service route
- [ ] `pulumi-oracle/compute.go` — remove stale `/data/postgres` setup (using Supabase not local PG)
- [ ] eBPF baseline — after Oracle VM deployment, verify ai-service only touches expected endpoints

---

## Deferred

- **Blue Button 2.0** — OAuth flow with CMS sandbox. Add after core Gemini endpoint is stable.
- **PATIENT role access** — patient-facing summaries with fhirId ownership check. Add after PROVIDER path is working.
- **Medications + observations** — Stage 1.5 after CSV import.
