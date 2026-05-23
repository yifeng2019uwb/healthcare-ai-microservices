# Healthcare AI Microservices Platform

HIPAA-aware healthcare platform built on Spring Boot microservices, deployed on a dedicated VM via Docker Compose.

## Architecture

```
Internet
    │
    ▼
Gateway  :8080
  · RS256 JWT validation (JWKS from auth-service)
  · Path-based RBAC (PATIENT / PROVIDER / ADMIN roles)
  · Injects X-User-Id / X-Username / X-User-Role headers
    │
    ├──▶ Auth Service      :8082  /api/auth/**
    ├──▶ Patient Service   :8081  /api/patients/**
    └──▶ Provider Service  :8083  /api/provider/**  /api/admin/**
    │
    ▼
Supabase PostgreSQL
```

## Services

| Service | Port | Status | Description |
|---------|------|--------|-------------|
| gateway | 8080 | deployed | JWT validation, path-based RBAC, header injection |
| auth-service | 8082 | deployed | Register, login, refresh, logout, JWKS endpoint |
| patient-service | 8081 | deployed | Patient profile, encounters, conditions, allergies |
| provider-service | 8083 | deployed | Provider profile, patient management, admin data import |
| shared | — | library | JPA entities, DAOs, enums, security constants |
| appointment-service | — | deferred | Booking — code exists, not deployed |
| ai-service | — | planned | Clinical summarization, risk analysis |

## Stack

- **Runtime**: Java 21, Spring Boot 3.4.4
- **Gateway**: Spring Cloud Gateway
- **Auth**: RS256 JWT, JWKS endpoint, role-based header injection
- **Database**: Supabase PostgreSQL
- **Deploy**: Docker Compose on dedicated VM
- **Test data**: Synthea synthetic patient data (HIPAA-safe)

## Local Development

```bash
cd services

# Build all services
./dev.sh all build

# Run unit tests
./dev.sh all test

# Run a single service locally
./dev.sh auth-service run
```

## Deploy (VM)

Services run as Docker containers managed by Compose:

```bash
cd docker
docker-compose up --build -d
```

## Database Schema

```bash
cd healthcare-infra/schema

# Deploy all tables (safe to re-run — IF NOT EXISTS guards)
DATABASE_URL="postgresql://postgres:<password>@db.<ref>.supabase.co:5432/postgres" \
  ./run-schema.sh

# Deploy a single table
DATABASE_URL="..." ./run-schema.sh encounters
```

## Integration Tests

Tests run against the live gateway via RestAssured. See [Integration Test Guide](docs/INTEGRATION_TEST_PLAN.md).

```bash
cd integration_tests

# Run all suites
./run-it.sh all

# Run a specific suite
./run-it.sh auth
./run-it.sh register
./run-it.sh patient
./run-it.sh provider
./run-it.sh admin

# Override gateway URL (default: http://localhost:8080)
GATEWAY_URL=http://<vm-ip>:8080 ./run-it.sh all
```

## Docs

- [`docs/`](docs/) — service design docs, ADRs, roadmap
- [`healthcare-infra/`](healthcare-infra/) — DB schema, Synthea data
- [`integration_tests/`](integration_tests/) — RestAssured black-box tests
- [`docker/`](docker/) — Docker Compose deployment
