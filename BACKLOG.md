# Healthcare AI Microservices — Backlog

## Tech Debt

| # | Issue | Found | Priority |
|---|-------|-------|----------|
| TD-4 | Patient ID enumeration — `GET /provider/patients/{id}` returns 403 for existing patients the provider can't access, leaking that the patient ID is valid. Should return 404 in both cases (no access and non-existent) so attackers can't probe which IDs exist. | 2026-04-30 | Medium |
| TD-6 | `testpatient02` not seeded — cross-patient isolation test in `PatientEncountersIT` is ABORTED. Register second patient (username: `testpatient02`, MRN: `MRN-000003`) via `POST /api/auth/register/patient`. | 2026-05-01 | Low |

---

## Next Up

- [ ] **testpatient02** — seed second test patient account to unblock cross-patient isolation tests (TD-6)
- [ ] **eBPF EDR on healthcare VM** — deploy eBPF agent on Oracle VMs, pattern already established for Docker VM and GKE.
- [ ] **AI governance** — archive/restore UI for `ai_analysis_results`; patient-facing summary endpoint with fhirId ownership check (PATIENT role). See `docs/ai-service-discussion.md`.
- [ ] **Debezium + Kafka auto-trigger** — Debezium Server watching conditions/allergies WAL → Redpanda → ai-service consumer. Currently manual on-demand only. See `docs/ai-service-discussion.md` for design.
- [ ] **Auto-restart on VM reboot** — add `restart: unless-stopped` to all services in compose-gateway.yml and compose-backend.yml so containers start automatically after VM reboot.
- [ ] **JAR upload reliability** — current `rsync` approach works but is slow for large JARs (~60MB each). Consider building Docker images locally and loading on VM, or using a private registry.

---

## Maintenance Rules

- Completed tasks move to `DAILY_WORK_LOG.md` (date, what was done, why)
- Stale tasks (superseded, irrelevant) are deleted — not archived here
- Tech debt items stay until resolved or explicitly dropped
