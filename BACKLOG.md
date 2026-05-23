# Healthcare AI Microservices — Backlog

## Tech Debt

| # | Issue | Found | Priority |
|---|-------|-------|----------|
| TD-4 | Patient ID enumeration — `GET /provider/patients/{id}` returns 403 for existing patients the provider can't access, leaking that the patient ID is valid. Should return 404 in both cases (no access and non-existent) so attackers can't probe which IDs exist. | 2026-04-30 | Medium |
| TD-6 | `testpatient02` not seeded — cross-patient isolation test in `PatientEncountersIT` is ABORTED. Register second patient (username: `testpatient02`, MRN: `MRN-000003`) via `POST /api/auth/register/patient`. | 2026-05-01 | Low |

---

## Next Up

- [ ] **testpatient02** — seed second test patient account to unblock cross-patient isolation tests (TD-6)
- [ ] **AI service** — Java Spring Boot, RabbitMQ pub/sub trigger (provider publishes → AI consumes), Vertex AI Gemini, `ai_analysis_results` table, PROVIDER-only route `/api/ai/**`. Design finalized in `docs/ai-service-discussion.md`.
- [ ] **eBPF EDR on healthcare VM** — deploy eBPF agent on `healthcare-ai-yifeng` VM, grant compute SA `logging.logWriter` on `ebpfagent` project via Pulumi `infra/` stack, add project to `infra/main.go`. Pattern already established for Docker VM and GKE.

---

## Maintenance Rules

- Completed tasks move to `DAILY_WORK_LOG.md` (date, what was done, why)
- Stale tasks (superseded, irrelevant) are deleted — not archived here
- Tech debt items stay until resolved or explicitly dropped
