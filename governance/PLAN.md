# Governance & AI — Implementation Plan

High-level plan. Details defined per phase when implementation starts.

---

## Phase 1 — Governance Service (build first)

New microservice at `governance/dashboard/` (Go/Gin).

**Why Go:** Pure API service — audit log writes, model registry CRUD, dashboard data. No ML needed. Go is fast, reliable, and strongly typed — correct for audit systems. Python only where ML libraries are required.

**Why first:** Audit log must exist before AI service goes live. Predictions without an audit trail violate NIST MANAGE.

**Core responsibilities:**
- Audit log — record every AI prediction (patient, model, result, timestamp, provider)
- Model registry — track deployed models, versions, owner, status
- Dashboard — visualize all four NIST AI RMF functions: GOVERN, MAP, MEASURE, MANAGE

**Key design decisions:**
- Lives in `governance/dashboard/`, not under `services/` — governance is a first-class concern
- DAO layer for all data access — consistent with rest of platform
- AI service calls governance audit endpoint on every prediction

---

## Phase 2 — Synthea Data Ingestion API (data foundation for AI)

Admin API endpoint for loading Synthea synthetic patient data. Repeatable — used multiple times as AI model is tuned.

**Why DAO not script:** Keeps architecture consistent, testable, and callable as an API for future reloads.

**Core responsibilities:**
- Accept Synthea data and load through DAO layer into existing patient/encounter/conditions tables
- Run NIST data quality checks: completeness, consistency, representativeness
- Write a row to `data_ingestion_log` with: source, Synthea version, record count, load date, quality check results
- Governance dashboard reads `data_ingestion_log` — satisfies MAP (data lineage)

**Key design decisions:**
- Endpoint: `POST /api/admin/data/ingest` — admin role only
- `DataIngestionLog` entity + repository in shared module
- Repeatable: each load creates a new log entry, old data is not overwritten

---

## Phase 3 — AI API (readmission risk model)

New AI microservice (Python/FastAPI) at `services/ai-service/`. Python chosen specifically for ML libraries (sklearn, pandas, numpy) — no equivalent in Go.

**Why last:** Depends on governance audit log (Phase 1) and training data (Phase 2).

**Core responsibilities:**
- Readmission risk model — predict 30-day readmission risk from patient history
- Every prediction calls governance audit log automatically
- Model versioned and registered in governance model registry before deployment

**Key design decisions:**
- First model: readmission risk (sklearn, logistic regression) — simple, well-understood, known bias risks
- Vertex AI Gemini deferred — start simple, upgrade later
- No prediction is made without a corresponding audit log entry

---

## Language Summary

| Service | Language | Reason |
|---------|----------|--------|
| auth, gateway, provider, patient, appointment | Java / Spring Boot | Existing |
| governance dashboard | Go / Gin | Pure API, audit reliability, strong typing |
| AI service | Python / FastAPI | ML libraries (sklearn, pandas, numpy) |

---

## Dependencies

```
Phase 1 (Governance — Go)
    └── Phase 2 (Synthea ingestion — Java) ── training data
            └── Phase 3 (AI API — Python) ── calls governance audit log
```

---

## Infrastructure Changes

Minimal — reuses existing GCP setup.

| Change | Why |
|--------|-----|
| 2 new Cloud Run services | Governance (Go) + AI service (Python) — same Terraform pattern as existing 5 services |
| 2 new gateway routes | `/api/governance/**` and `/api/ai/**` — same pattern as existing routes |
| 1 new GCS bucket | Store trained sklearn model file (`.pkl`) — AI service loads model from GCS at startup, makes retraining and versioning easier |
| New DB tables | `data_ingestion_log`, `audit_log`, `model_registry` — Flyway migrations, no Terraform needed |

Not needed: new Cloud SQL, Redis, Secret Manager secrets, or VPC changes.

---

## Future

- NIST AI RMF governance pattern may be applied to eBPF EDR project as well
