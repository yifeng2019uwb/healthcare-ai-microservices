# Healthcare AI Microservices Platform

HIPAA-aware healthcare platform built on Spring Boot microservices, deployed on GCP Cloud Run.

## Architecture

```
Internet
    │
    ▼
Gateway (Cloud Run, public)
  · JWT validation (RS256)
  · Redis token blacklist
  · Injects X-User-Id / X-Username / X-User-Role headers
    │
    ├──▶ Auth Service     (Cloud Run, internal)  /api/auth/**
    ├──▶ Patient Service  (Cloud Run, internal)  /api/patients/**
    └──▶ (more services)
    │
    ▼
Cloud SQL PostgreSQL  +  Redis (Memorystore)
```

## Services

| Service | Status | Description |
|---------|--------|-------------|
| gateway | ✅ deployed | JWT auth, routing |
| auth-service | ✅ deployed | Register, login, refresh, logout |
| patient-service | ✅ deployed | Patient profile, encounters, conditions, allergies |
| provider-service | ⏳ planned | Provider profiles |
| shared | ✅ | JPA entities, DAOs, enums — shared library |

## Stack

- **Runtime**: Java 17, Spring Boot 3.2
- **Cloud**: GCP Cloud Run, Cloud SQL (PostgreSQL), Memorystore (Redis), Artifact Registry
- **Infra**: Terraform
- **Auth**: RS256 JWT, JWKS endpoint, Redis blacklist
- **Data**: Synthea synthetic patient data (HIPAA-safe)

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

## Full CI Pipeline

```bash
./scripts/local-ci.sh --build --test       # before push
./scripts/local-ci.sh --deploy             # deploy all
./scripts/local-ci.sh --all               # full pipeline
```

## Docs

- [`docs/`](docs/) — service design docs
- [`healthcare-infra/`](healthcare-infra/) — Terraform, DB schema, Synthea data
- [`scripts/`](scripts/) — CI/CD and deploy scripts
