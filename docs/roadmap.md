# Project Roadmap

> Last Updated: April 2026

---

## Completed

- [x] GCP infrastructure (Cloud SQL, Cloud Run, VPC, Secret Manager, Redis)
- [x] Synthea data loaded — 200 Washington state patients
- [x] Shared module — entities, DAOs, enums, constants
- [x] Auth service — patient/provider register, login, refresh, logout, JWKS endpoint
- [x] Gateway — RS256 JWT validation, Redis blacklist, routing
- [x] Patient service — profile, update, encounters, conditions, allergies (5 endpoints)
- [x] Provider service — profile, register patient, patient list/detail, conditions, allergies (6 endpoints)
- [x] Appointment service — patient encounters, provider encounters, patient-by-provider encounters (5 endpoints)
- [x] Audit logging wired on every request across all services
- [x] Unit tests for all services (patient, provider, appointment)
- [x] Deploy scripts for all services

---

## Phase 2 — CMS Interview Prep

**Goal**: Add federally-recognized standards and CMS API integrations that directly signal domain knowledge for GS-2210 / CMS contractor roles.

- [x] **FHIR R4 responses** — same endpoints, second response format via content negotiation
  - Client sends `Accept: application/fhir+json` → gets FHIR R4 shape
  - Client sends `Accept: application/json` → gets existing response (default)
  - Applies to: `GET /api/patients/me`, `/me/encounters`, `/me/conditions`, `/me/allergies`
  - Implementation: `FhirMapper.java` + second `@GetMapping(produces="application/fhir+json")` per endpoint in patient-service. No changes to service layer or DAOs.
  - FHIR resources covered: `Patient`, `Encounter`, `Condition`, `AllergyIntolerance`
  - Demonstrates knowledge of CMS Interoperability and Patient Access Rule (CMS-9115-F)

- [ ] **RBAC enforcement at gateway** — reject requests where JWT role doesn't match path
  - PATIENT role blocked from `/api/provider/**` and `/api/appointments/provider/**`
  - PROVIDER role blocked from `/api/patients/**` and `/api/appointments/me/**`
  - Closes current security gap

---

## Phase 3 — Security Hardening

**Goal**: Production-grade security posture. Directly relevant to federal security and DevSecOps roles.

- [ ] **Cloud Armor WAF** — OWASP Top 10 blocking policy on the load balancer
- [ ] **OWASP ZAP scan** — automated security scan added to GitHub Actions CI/CD pipeline
- [ ] **STRIDE threat model** — written document covering all services and data flows
- [ ] **Cloud Audit Logs alerting** — GCP alerting policy on suspicious audit log patterns
- [ ] **Secret Manager audit** — verify all credentials are in Secret Manager, none in env vars

---

## Phase 4 — AI Layer

**Goal**: Vertex AI integration and CMS Blue Button demo.

- [ ] **Load medications + observations CSV** — Synthea data prerequisite for AI analysis
- [ ] **Blue Button 2.0 integration** — OAuth 2.0 flow with `sandbox.bluebutton.cms.gov`
  - Pull Medicare Part A/B/D claims for a patient
  - Map to existing Encounter/Condition model
  - Demonstrates hands-on experience with CMS's flagship patient data API
- [ ] **Vertex AI Gemini endpoint** — `POST /api/ai/analyze/{patientId}`
  - Clinical summarization and risk analysis using patient history
- [ ] **Demo video** — recorded walkthrough of full platform

---

## Notes

- FHIR R4 is the highest-priority item for CMS interviews — data model already maps cleanly
- Blue Button 2.0 is the most complex but most impressive CMS-specific work
- Phase 3 security items are good resume bullets for federal IT security roles regardless of interview timing
