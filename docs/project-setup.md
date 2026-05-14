# Project Setup & Workflow

> Platform: GCP (Cloud Run + Cloud SQL + Secret Manager)  
> Last Updated: 2026-05-13

---

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java | 21 | `brew install openjdk@21` |
| Maven | 3.8+ | `brew install maven` |
| gcloud CLI | latest | https://cloud.google.com/sdk/docs/install |
| Terraform | 1.5+ | `brew install terraform` |

---

## First-Time GCP Auth

```bash
gcloud auth login --no-launch-browser        # headless VM — always use this flag
gcloud config set project healthcare-ai-yifeng
gcloud auth application-default login --no-launch-browser
```

---

## Day-to-Day Workflow

### 1. Build a service locally

```bash
cd services/
./dev.sh gateway          # build one service
./dev.sh auth-service
./dev.sh patient-service
./dev.sh provider-service
```

### 2. Deploy to Cloud Run

```bash
# Deploy one service
./scripts/deploy-services.sh gateway

# Deploy multiple
./scripts/deploy-services.sh auth-service gateway

# Deploy everything (gateway always deploys last)
./scripts/deploy-services.sh all
```

> After any code change: build locally first, then deploy.

### 3. Run integration tests

Tests run against the live GCP gateway URL.

```bash
cd integration_tests/

./run-it.sh seed          # verify test accounts are reachable
./run-it.sh auth          # auth endpoints (login, refresh, logout)
./run-it.sh register      # patient registration
./run-it.sh patient       # patient profile + encounters
./run-it.sh provider      # provider profile + patient access
./run-it.sh all           # run everything in order
```

Override gateway URL if needed:
```bash
GATEWAY_URL=https://your-gateway-url.run.app ./run-it.sh all
```

### 4. Infra changes (Terraform)

```bash
./scripts/local-ci.sh --terraform              # plan only (safe — preview changes)
./scripts/local-ci.sh --terraform --apply      # plan + apply to GCP
```

> Always run plan first and review output before applying.

---

## Current Service URLs (dev)

| Service | Cloud Run URL |
|---|---|
| Gateway | `https://gateway-dev-824144893232.us-west1.run.app` |
| Auth | resolved by gateway at deploy time |
| Patient | resolved by gateway at deploy time |
| Provider | resolved by gateway at deploy time |

---

## Common Scenarios

### Changed gateway code (e.g. RBAC, JwtAuthFilter)
```bash
cd services/ && ./dev.sh gateway
./scripts/deploy-services.sh gateway
./integration_tests/run-it.sh all
```

### Changed auth-service code
```bash
cd services/ && ./dev.sh auth-service
./scripts/deploy-services.sh auth-service
./integration_tests/run-it.sh auth
```

### Changed provider-service code
```bash
cd services/ && ./dev.sh provider-service
./scripts/deploy-services.sh provider-service
./integration_tests/run-it.sh provider
```

### Changed Terraform infra
```bash
./scripts/local-ci.sh --terraform          # review plan first
./scripts/local-ci.sh --terraform --apply  # apply after review
```

### Changed DB schema (add migration)
```bash
# Add new file: healthcare-infra/schema/migrations/V00X__description.sql
# Apply manually via psql or Cloud SQL Studio
```

---

## Test Accounts (GCP dev)

| Role | Username | Password | Linked record |
|---|---|---|---|
| PROVIDER | `drDeckow` | `Password1@` | PRV-000001 (Louann705 Deckow585) |
| PATIENT | `testpatient01` | `Password1@` | MRN-000002 (Carly657 Pollich983) |

---

## Key Files

| File | Purpose |
|---|---|
| `services/dev.sh` | Build a service locally |
| `scripts/deploy-services.sh` | Deploy to Cloud Run |
| `integration_tests/run-it.sh` | Run integration tests |
| `healthcare-infra/terraform/` | GCP infrastructure |
| `healthcare-infra/schema/sql/` | Table definitions |
| `healthcare-infra/schema/migrations/` | Schema migrations |
| `docs/gateway-service-design.md` | Gateway design + decisions |
| `docs/provider-service-design.md` | Provider + admin design |
| `docs/auth-fhir-identity-tech-debt.md` | Auth/FHIR identity debt |
