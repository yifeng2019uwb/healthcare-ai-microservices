# Healthcare AI Microservices Platform

HIPAA-aware healthcare platform built on Spring Boot microservices, deployed on GCP Cloud Run.

## Architecture

```
Internet
    │
    ▼
Gateway (Cloud Run, public)
  · RS256 JWT validation (JWKS from auth-service)
  · Path-based RBAC (PATIENT / PROVIDER / ADMIN roles)
  · Injects X-User-Id / X-Username / X-User-Role / X-Fhir-Id headers
    │
    ├──▶ Auth Service      (Cloud Run, internal)  /api/auth/**
    ├──▶ Patient Service   (Cloud Run, internal)  /api/patients/**
    ├──▶ Provider Service  (Cloud Run, internal)  /api/provider/**  /api/admin/**
    └──▶ (AI Service — planned)                  /api/ai/**
    │
    ▼
Cloud SQL PostgreSQL
```

## Services

| Service | Status | Description |
|---------|--------|-------------|
| gateway | ✅ deployed | JWT validation, path-based RBAC, header injection |
| auth-service | ✅ deployed | Register, login, refresh, logout, JWKS endpoint |
| patient-service | ✅ deployed | Patient profile, encounters, conditions, allergies |
| provider-service | ✅ deployed | Provider profile, patient list/detail, conditions, allergies, admin import |
| shared | ✅ library | JPA entities, DAOs, enums, security constants |
| appointment-service | ⏳ deferred | Encounter history — code exists, not deployed (deprioritized for AI layer) |
| ai-service | 🔜 planned | Vertex AI Gemini — clinical summarization, risk analysis |

## Stack

- **Runtime**: Java 17, Spring Boot 3.2
- **Cloud**: GCP Cloud Run, Cloud SQL (PostgreSQL), Artifact Registry, Secret Manager
- **Infra**: Terraform
- **Auth**: RS256 JWT, JWKS endpoint, `fhirId` claim (patients.id / providers.id)
- **Data**: Synthea synthetic patient data (200 patients, HIPAA-safe)

## Local Development

```bash
cd services

# build all
./dev.sh all build

# test
./dev.sh shared test

# run a service locally
./dev.sh auth-service run
```

## Deploy

```bash
# deploy one service
./scripts/deploy-services.sh patient-service

# deploy multiple
./scripts/deploy-services.sh auth-service patient-service

# deploy everything
./scripts/deploy-services.sh all
```

## Integration Tests

```bash
cd integration_tests

# run all tests
./run-it.sh all

# run by suite
./run-it.sh auth
./run-it.sh patient
./run-it.sh provider
./run-it.sh admin
```

## Full CI Pipeline

```bash
./scripts/local-ci.sh --build --test       # before push
./scripts/local-ci.sh --deploy             # deploy all
./scripts/local-ci.sh --all               # full pipeline
```

## Docs

- [`docs/`](docs/) — service design docs, roadmap, AI service discussion
- [`healthcare-infra/`](healthcare-infra/) — Terraform, DB schema, Synthea data
- [`scripts/`](scripts/) — CI/CD and deploy scripts
