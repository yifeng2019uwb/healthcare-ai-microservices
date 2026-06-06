# Project Roadmap

> Last Updated: 2026-05-23

---

## Completed

- [x] Synthea data loaded — 200 Washington state patients
- [x] Shared module — entities, DAOs, enums, constants
- [x] Auth service — patient/provider register, login, refresh, logout, JWKS endpoint
- [x] Auth service — removed Redis token blacklist (tokens expire via JWT TTL)
- [x] Auth service — `fhirId` JWT claim (patients.id / providers.id injected at login)
- [x] Gateway — RS256 JWT validation, path-based RBAC, `X-Fhir-Id` header injection
- [x] Patient service — profile, update, encounters, conditions, allergies (5 endpoints)
- [x] Provider service — profile, register patient, patient list/detail, conditions, allergies (6 endpoints)
- [x] Provider service — org name + display name populated on register
- [x] Audit logging wired on every request across all services
- [x] Unit tests for all services (patient, provider)
- [x] Integration tests — auth, patient profile, provider profile, admin import
- [x] Schema indexes — patient history, audit log, encounter, condition, allergy lookups
- [x] Deployment — GKE (`health-ai-cluster-us-west1`), Supabase PostgreSQL (previously Oracle OCI VM — account terminated 2026-06-06, see `legacy/`)
- [x] Infrastructure as Code — `kubernetes/pulumi/` (GKE cluster, Artifact Registry, Workload Identity)

## Deferred (not removed — may revisit)

- [ ] **Appointment service** — stub exists in `services/appointment-service/` but not deployed.
  Re-enable when needed: add to `docker-compose.yml` + restore encounter integration tests.

---

## Phase 2 — CMS Interview Prep

**Goal**: Add federally-recognized standards and CMS API integrations that directly signal domain knowledge for GS-2210 / CMS contractor roles.

- [ ] **FHIR R4 responses** — same endpoints, second response format via content negotiation
  - Client sends `Accept: application/fhir+json` → gets FHIR R4 shape
  - Client sends `Accept: application/json` → gets existing response (default)
  - Applies to: `GET /api/patients/me`, `/me/encounters`, `/me/conditions`, `/me/allergies`
  - Implementation: `FhirMapper.java` + second `@GetMapping(produces="application/fhir+json")` per endpoint in patient-service. No changes to service layer or DAOs.
  - FHIR resources covered: `Patient`, `Encounter`, `Condition`, `AllergyIntolerance`
  - Demonstrates knowledge of CMS Interoperability and Patient Access Rule (CMS-9115-F)

- [x] **RBAC enforcement at gateway** — reject requests where JWT role doesn't match path
  - PATIENT role blocked from `/api/provider/**` and `/api/encounters/provider/**`
  - PROVIDER role blocked from `/api/patients/**` and `/api/encounters/me/**`
  - Implemented in `JwtAuthFilter` with prefix-matched `role-paths` config

---

## Phase 3 — AI Layer

**Goal**: Gemini AI integration and CMS Blue Button demo. Design finalized in `docs/ai-service-discussion.md`.

- [ ] **`pulumi-supabase/`** — Go Pulumi stack: `postgresql.Publication` + `postgresql.ReplicationSlot`
- [ ] **Debezium Server** — `docker/debezium/application.properties` config, add to `docker-compose.yml`
- [ ] **Redpanda** — Kafka-compatible broker, add to `docker-compose.yml`
- [ ] **ai-service** — Spring Boot module: `@KafkaListener`, debounce check, Gemini REST call, DB write
- [ ] **`ai_analysis_results` table** — already defined in `healthcare-infra/schema/sql/`, deploy via `run-schema.sh`
- [ ] **Gateway route** — add `/api/ai/**` → PROVIDER role + ai-service upstream
- [ ] **Load medications + observations CSV** — Synthea data, extends AI prompt context (Stage 1.5)
- [ ] **Blue Button 2.0 integration** — OAuth 2.0 flow with `sandbox.bluebutton.cms.gov`
  - Pull Medicare Part A/B/D claims for a patient
  - Demonstrates hands-on experience with CMS's flagship patient data API

---

## Phase 4 — Security Hardening

**Goal**: Production-grade security posture. Directly relevant to federal security and DevSecOps roles.

- [ ] **eBPF EDR on GKE** — file-based rules (V3/V7/V9) not yet passing on health-ai GKE; diagnose opensnoop file sensor
- [ ] **OWASP ZAP scan** — automated security scan added to GitHub Actions CI/CD pipeline
- [ ] **STRIDE threat model** — written document covering all services and data flows
- [ ] **TD-4 fix** — `GET /provider/patients/{id}` should return 404 for both non-existent and inaccessible patients (currently leaks valid IDs via 403 vs 404)

---

## Notes

- FHIR R4 is the highest-priority item for CMS interviews — data model already maps cleanly
- Blue Button 2.0 is the most complex but most impressive CMS-specific work
- Phase 3 security items are good resume bullets for federal IT security roles regardless of interview timing
- Gemini API key (Google AI Studio) is acceptable for synthetic Synthea data — no HIPAA BAA needed at this stage
