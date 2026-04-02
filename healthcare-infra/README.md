# Healthcare Infrastructure

GCP infrastructure managed with Terraform + Cloud SQL schema scripts.

## What's Running

| Component | Service | Notes |
|-----------|---------|-------|
| Database | Cloud SQL PostgreSQL (healthcare-db-dev) | Private IP, VPC only |
| Cache | Memorystore Redis | Token blacklist |
| Network | VPC + Serverless VPC connector | Services → DB |
| Secrets | Secret Manager | db-password, jwt-private-key, jwt-public-key |
| Registry | Artifact Registry | Docker images |
| IAM | Cloud Run service account | Least-privilege |

## Directory Structure

```
healthcare-infra/
├── terraform/          # GCP infra — VPC, Cloud SQL, Redis, IAM, Artifact Registry
│   ├── run-terraform.sh
│   └── *.tf
├── schema/             # PostgreSQL DDL
│   ├── run-schema.sh
│   └── sql/            # one file per table
└── synthea/            # Synthetic patient data (HIPAA-safe test data)
    └── synthea-with-dependencies.jar
```

## Common Commands

```bash
# Plan infra changes (safe, no changes applied)
./scripts/local-ci.sh --terraform

# Apply infra changes
./scripts/local-ci.sh --terraform --apply

# Deploy DB schema
./scripts/local-ci.sh --schema

# Generate + load Synthea test data
./scripts/local-ci.sh --data
```

## Cloud Run Services

Managed by `scripts/deploy-services.sh` — NOT by Terraform.
Terraform only manages IAM bindings for Cloud Run (not the services themselves).
