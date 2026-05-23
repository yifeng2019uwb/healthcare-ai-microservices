# System Design

> Version: 4.0 | Last Updated: May 2026

---

## Overview

Healthcare API platform serving synthetic patient data with production-grade security.
Built with Spring Boot microservices deployed via Docker Compose on a single VM,
backed by Supabase PostgreSQL.

---

## Architecture

```
Internet
    │
    ▼
VM — Docker Compose
    │
    ▼
Gateway (port 8080)         ← RS256 JWT validation, RBAC routing
    │
    ├─────────────────────────────────────────┐
    │                   │                     │
    ▼                   ▼                     ▼
auth-service        patient-service      provider-service
(port 8082)         (port 8081)          (port 8083)
    │                   │                     │
    └───────────────────┴─────────────────────┘
                         │
                         ▼
                 Supabase PostgreSQL
                 (managed — no VPC)
```

---

## Services

| Service | Port | Status | Responsibility |
|---------|------|--------|----------------|
| gateway | 8080 | deployed | RS256 JWT validation, routing all `/api/**` |
| auth-service | 8082 | deployed | register, login, refresh, logout, JWKS |
| patient-service | 8081 | deployed | patient profile, encounters, conditions, allergies |
| provider-service | 8083 | deployed | provider profile, patient management, admin import |
| appointment-service | — | deferred | booking stub, not deployed |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Compute | Docker Compose on GCP VM |
| Database | Supabase PostgreSQL |
| Auth | RS256 JWT — self-issued by auth-service, validated at gateway |
| Services | Spring Boot 3.4.4 / Java 21 |
| Data | Synthea synthetic patient data (CSV import) |
| CI | GitHub Actions |

---

## Security Layers

```
Layer 1 — Auth:    RS256 JWT validation at gateway (self-issued, JWKS endpoint)
Layer 2 — RBAC:    Role-based path enforcement at gateway (PATIENT/PROVIDER/ADMIN — prefix-matched in JwtAuthFilter)
Layer 3 — Audit:   audit_logs table — append-only, every PHI access logged
Layer 4 — Input:   Bean Validation on all request DTOs
Layer 5 — Data:    All credentials injected via Docker Compose env vars (not hardcoded)
```

Gateway RBAC (Layer 2) is tracked in tech debt TD-3 — currently not fully enforced.

---

## Data

Synthea synthetic patient data — open-source generator from MITRE, used by CMS
and federal health IT programs. ~200 Washington state patients loaded into Supabase.

See `docs/database-design.md` for schema details.
See `healthcare-infra/` for schema SQL files and Synthea generation scripts.

---

## Deployment

Services are deployed as Docker containers via `docker/docker-compose.yml`.
Each service connects to Supabase via injected environment variables:

```
SPRING_DATASOURCE_URL      — jdbc:postgresql://db.<ref>.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME — postgres
SPRING_DATASOURCE_PASSWORD — <password>
```

See `docker/README.md` for deployment commands.
