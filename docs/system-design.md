# System Design

> Version: 3.0 | Last Updated: March 2026
> Previous versions archived in `docs/archive/`

---

## Overview

A cloud-native healthcare API platform on GCP serving synthetic patient data
with production-grade security. Built with Spring Boot microservices, Firebase
Auth, and Synthea-generated data.

---

## Architecture

```
Internet
    │
    ▼
Cloud Armor (WAF)           ← OWASP Top 10 blocking
    │
    ▼
Cloud Run — API Gateway     ← RS256 JWT validation, routing
    │
    ├──────────────────────────────────┐
    ▼                                  ▼
Cloud Run — Patient Service    Cloud Run — Appointment Service
    │                                  │
    └──────────────┬───────────────────┘
                   ▼
           Cloud SQL PostgreSQL
           (Private VPC — no public IP)

GCP Secret Manager  ← All credentials
Cloud Logging       ← Structured logs
Cloud Audit Logs    ← GCP API audit trail
```

---

## Services

| Service | Phase | Responsibility |
|---|---|---|
| API Gateway | 1 | Routing, Firebase JWT validation |
| Patient Service | 1 | Patient profile, medical history |
| Appointment Service | 1 | Browse and book encounters |
| Provider Service | 2 | Provider profiles, RBAC |
| AI Service | 2 | Vertex AI Gemini patient analysis |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Cloud | GCP (us-west1) |
| Compute | Cloud Run |
| Database | Cloud SQL PostgreSQL 15 |
| Auth | RS256 JWT (self-issued, JWKS endpoint) |
| Secrets | GCP Secret Manager |
| WAF | Cloud Armor |
| IaC | Terraform |
| CI/CD | GitHub Actions + Workload Identity Federation |
| Services | Spring Boot 3.2 / Java 17 |
| Data | Synthea synthetic patient data (CSV) |

---

## Security Layers

```
Layer 1 — Network:   Cloud Armor WAF
Layer 2 — Auth:      RS256 JWT validation at Gateway (self-issued)
Layer 3 — Access:    RBAC (patient Phase 1, provider Phase 2)
Layer 4 — Secrets:   GCP Secret Manager
Layer 5 — Data:      Cloud SQL in private VPC, TLS in transit
Layer 6 — Audit:     audit_logs table + Cloud Audit Logs
Layer 7 — Scan:      OWASP ZAP in CI/CD pipeline
```

---

## Data

Synthea synthetic patient data — open-source generator from MITRE, used by
CMS and federal health IT programs. 200 Washington state patients loaded into
Cloud SQL.

See `docs/database-design.md` for schema details.

---

## Infrastructure

GCP Project: `healthcare-ai-yifeng` | Region: `us-west1`

Terraform manages all GCP resources. State stored in GCS bucket.
CI/CD uses Workload Identity Federation — no service account key files.

See `healthcare-infra/` for Terraform configs and deployment scripts.

---

## Phase Status

- [x] Phase 1 — GCP infrastructure (Cloud SQL, Cloud Run, Secret Manager, VPC)
- [x] Phase 1 — Synthea data loaded into Cloud SQL
- [x] Phase 1 — Gateway deployed to Cloud Run (RS256 JWT, Redis blacklist)
- [x] Phase 1 — Auth service deployed (register, login, refresh, logout)
- [x] Phase 1 — Patient service deployed (profile, encounters, conditions, allergies)
- [ ] Phase 2 — Provider service + RBAC
- [ ] Phase 2 — Cloud Armor WAF, OWASP ZAP
- [ ] Phase 3 — Vertex AI Gemini analysis endpoint