# Healthcare AI Microservices Platform

HIPAA-aware healthcare platform built on Spring Boot microservices, deployed on a dedicated VM via Docker Compose.

## Architecture

```
Internet
    │
    ▼
Gateway  :8080
  · RS256 JWT validation (JWKS from auth-service)
  · Specific path-based routing (unknown paths → 404 at gateway, not forwarded)
  · RBAC: PATIENT / PROVIDER / ADMIN roles
  · Injects X-User-Id / X-Username / X-User-Role / X-Fhir-Id headers
    │
    ├──▶ Auth Service      :8082  /api/auth/login|refresh|logout|register/**
    ├──▶ Provider Service  :8083  /api/provider/**  /api/admin/**
    ├──▶ AI Service        :8085  /api/ai/**
    └──▶ Appointment Svc   :8084  /api/encounters/**  (limited — read only)
    │
    ▼
PostgreSQL (Supabase)
```

## Services

| Service | Port | Status | Description |
|---------|------|--------|-------------|
| gateway | 8080 | deployed | JWT auth, path-based RBAC, header injection |
| auth-service | 8082 | deployed | Register, login, refresh, logout, JWKS |
| provider-service | 8083 | deployed | Provider profile, patient management, conditions/allergies write, admin import |
| ai-service | 8085 | deployed | On-demand clinical summarization and risk analysis (Gemini) |
| appointment-service | 8084 | partial | Encounter read endpoints; booking deferred |
| patient-service | 8081 | not deployed | Patient self-service; excluded from current Docker Compose |
| shared | — | library | JPA entities, DAOs, enums, security constants |

## Stack

- **Runtime**: Java 21, Spring Boot 3.4.4
- **Gateway**: Spring Cloud Gateway (specific path predicates — no wildcard routes)
- **Auth**: RS256 JWT, JWKS endpoint, role-based header injection
- **AI**: Google Gemini via REST (`gemini-2.5-flash`, fallback `gemini-1.5-flash`)
- **Database**: Supabase PostgreSQL
- **Deploy**: GKE (`health-ai-cluster-us-west1`) via Kubernetes manifests + Pulumi
- **Test data**: Synthea synthetic patient data (HIPAA-safe)

## Local Development

```bash
cd services

# Build all services
./dev.sh all build

# Package a service (produces fat jar for Docker)
./dev.sh <service> package

# Run unit tests
./dev.sh all test

# Run a single service test
./dev.sh provider-service test
```

## Deploy (VM via Docker Compose)

```bash
# Copy and fill in secrets
cp docker/.env.example docker/.env   # set DB credentials, JWT keys, GEMINI_API_KEY

# Build jars first (Docker copies from target/)
cd services
./dev.sh auth-service package
./dev.sh provider-service package
./dev.sh ai-service package
./dev.sh gateway package

# Start all services
cd ..
make start

# Stop all services
make stop
```

Environment variables required in `docker/.env`:

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | DB password |
| `JWT_PRIVATE_KEY` | RS256 private key (PKCS8, base64) |
| `JWT_PUBLIC_KEY` | RS256 public key (base64) |
| `JWT_KEY_ID` | JWKS key ID (e.g. `auth-key-v1`) |
| `GEMINI_API_KEY` | Google Gemini API key |

## Database Schema

Schema files are idempotent — safe to re-run. Each file uses `CREATE TABLE IF NOT EXISTS` plus `ALTER TABLE ADD COLUMN IF NOT EXISTS` for migrations.

```bash
cd healthcare-infra/schema

# Deploy all tables
./run-schema.sh

# Deploy or migrate a single table
./run-schema.sh ai_analysis_results

# DATABASE_URL is auto-loaded from docker/.env if not set in environment
```

## Integration Tests

Tests run against the live gateway via RestAssured.

```bash
cd integration_tests

# Verify test accounts are reachable
./run-it.sh seed

# Individual suites
./run-it.sh auth
./run-it.sh register
./run-it.sh provider
./run-it.sh ai           # condition write + AI read (excludes live Gemini call)
./run-it.sh ai-live      # full AI suite including Gemini trigger (slow, costs quota)
./run-it.sh admin        # bulk CSV import (requires test-data CSV files)

# Run all (excludes patient — service not deployed; excludes ai-live)
./run-it.sh all

# Override gateway URL (default: http://localhost:8080)
GATEWAY_URL=http://<vm-ip>:8080 ./run-it.sh all
```

## Docs

- [`docs/`](docs/) — service design docs, ADRs, roadmap
- [`healthcare-infra/`](healthcare-infra/) — DB schema, Synthea data generation
- [`integration_tests/`](integration_tests/) — RestAssured black-box tests
- [`integration_tests/scripts/SIMULATE_AI.md`](integration_tests/scripts/SIMULATE_AI.md) — AI simulation design (bulk condition/allergy write + AI trigger)
- [`docker/`](docker/) — Docker Compose deployment
