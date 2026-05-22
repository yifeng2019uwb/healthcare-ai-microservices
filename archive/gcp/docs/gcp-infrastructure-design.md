# GCP Infrastructure Design

> **Single source of truth for GCP infrastructure.**
> Companion to: `docs/HEALTHCARE_PLATFORM_DESIGN.md`
> Version: 1.0 | Status: Active | Last Updated: March 2026

---

## 1. Overview

### Environment

| Field | Value |
|---|---|
| Region | `us-west1` |
| Environment | `dev` (prod added later) |

---

## 2. Architecture Overview

Internet
    │
    ▼
Cloud Armor (WAF)
    │  OWASP Top 10 rules
    ▼
Cloud Load Balancer
    │
    ▼
Cloud Run — gateway
    │  Spring Cloud Gateway
    │  Firebase JWT validation
    │  Port 8080
    ├─────────────────────┐
    ▼                     ▼
Cloud Run               Cloud Run
patient-service         appointment-service
Port 8002               Port 8004
    │                     │
    └──────────┬──────────┘
               ▼
        Cloud SQL
        PostgreSQL 15
        (Private IP — VPC only)
               │
        Secret Manager
        (credentials injected
         at runtime)

Artifact Registry
(Docker images for all services)

Cloud Logging
Cloud Audit Logs

---

## 3. GCP Services

### 3.1 VPC Network

| Field                 | Value             |
|-----------------------|-------------------|
| Name                  | `healthcare-vpc`  |
| Region                | `us-west1`        |
| Subnet                | `healthcare-subnet` |
| Subnet CIDR           | `10.0.0.0/24`     |
| Private Google Access | Enabled           |

Private Google Access allows Cloud Run and Cloud SQL to reach GCP APIs
(Secret Manager, Artifact Registry) without a public IP.

### 3.2 Cloud SQL

| Field             | Value                             |
|-------------------|-----------------------------------|
| Instance name     | `healthcare-db-dev`               |
| Database version  | PostgreSQL 15                     |
| Tier              | `db-f1-micro` (free tier eligible) |
| Region            | `us-west1`                        |
| IP                | Private IP only (VPC-internal)    |
| Storage           | 10 GB SSD                         |
| Backups           | Disabled (dev environment)        |
| Database name     | `healthcare`                      |
| User              | `postgres`                        |
| Password          | Stored in Secret Manager          |

Private IP means Cloud SQL is not reachable from the internet.
Only resources inside `healthcare-vpc` can connect.
Local development connects via Cloud SQL Auth Proxy.

### 3.3 Artifact Registry

| Field | Value |
|---|---|
| Repository name | `healthcare` |
| Format | Docker |
| Region | `us-west1` |
| Full path | `us-west1-docker.pkg.dev/${project_id}/healthcare` |

All service Docker images are pushed here by Cloud Build and
pulled by Cloud Run at deploy time.

### 3.4 Secret Manager

Secrets stored — values never in code, config files, or environment variables.

| Secret Name | Content | Consumed By |
|---|---|---|
| `db-password` | Cloud SQL postgres password | All services via Cloud Run |
| `firebase-project-id` | Firebase project identifier | Gateway, services |
| `firebase-service-account` | Firebase Admin SDK JSON | Gateway |
| `jwt-secret` | Internal service signing key | Gateway, services |

All secrets use automatic replication. Services access them via
the Cloud Run `--set-secrets` flag — injected as environment variables
at container startup, never printed to logs.

### 3.5 Cloud Run Services

All services are deployed to Cloud Run (fully managed).
No VMs, no Kubernetes, no node management.

| Service | Image | Port | Auth | Phase |
|---|---|---|---|---|
| `gateway` | `healthcare/gateway` | 8080 | Public (Cloud Armor in front) | 1 |
| `patient-service` | `healthcare/patient-service` | 8080 | Internal only | 1 |
| `appointment-service` | `healthcare/appointment-service` | 8080 | Internal only | 1 |
| `provider-service` | `healthcare/provider-service` | 8080 | Internal only | 2 |
| `ai-service` | `healthcare/ai-service` | 8080 | Internal only | 2 |

**Gateway** is the only service with a public URL.
All other services use `--no-allow-unauthenticated` — only the gateway
can call them via internal Cloud Run service-to-service auth.

Cloud Run configuration (all services):
- Min instances: 0 (scales to zero when idle — saves cost)
- Max instances: 3 (dev environment limit)
- Memory: 512Mi
- CPU: 1
- Concurrency: 80

### 3.6 Cloud Armor

| Field         | Value                                     |
|---------------|-------------------------------------------|
| Policy name   | `healthcare-waf`                          |
| Attached to   | Cloud Load Balancer (in front of gateway) |
| Rules         | OWASP Top 10 preconfigured ruleset        |
| Mode          | Prevention (blocks, not just detects)     |

Blocks common attacks at the network edge before requests reach
the application layer.

### 3.7 IAM — Service Accounts

| Service Account | Email                       | Roles                         | Purpose                 |
|-----------------|-----------------------------|-------------------------------|-------------------------|
| `github-actions`|`github-actions@${project_id}|`roles/run.admin`,             |CI/CD via WIF            |
|                 |.iam.gserviceaccount.com`    |`roles/artifactregistry.writer`,|                        |
|                 |                             |`roles/secretmanager.secretAccessor`|                    |
-----------------------------------------------------------------------------------------------------------
| `cloud-run-sa`  |`cloud-run-sa@${project_id}  |`roles/cloudsql.client`,       | Runtime identity for all|
|                 |.iam.gserviceaccount.com`    |`roles/secretmanager.secretAccessor`| Cloud Run services |

All service accounts follow least privilege — only the roles they need.
No `roles/editor` or `roles/owner`.

### 3.8 Workload Identity Federation

Keyless authentication from GitHub Actions to GCP.
No service account JSON key file exists anywhere.

| Field         | Value                                         |
|---------------|-----------------------------------------------|
| Pool name     | `github-pool`                                 |
| Provider name | `github-provider`                             |
| Issuer        | `https://token.actions.githubusercontent.com` |
| Allowed repo  | `yifeng2019uwb/healthcare-ai-microservices`   |

See `docs/CICD_SECURITY.md` for full setup details.

### 3.9 Cloud Logging + Cloud Audit Logs

| Service           | What it captures                                        |
|-------------------|---------------------------------------------------------|
| Cloud Logging     | Structured application logs from all Cloud Run services |
| Cloud Audit Logs  | All GCP API calls — who did what and when               |

Both are enabled by default on new GCP projects.
Application logs use structured JSON format for easy querying.

---

## 4. Networking

```
Internet
    │
    ▼ (public)
Cloud Armor + Load Balancer
    │
    ▼ (public URL)
Cloud Run — gateway
    │
    ▼ (internal VPC)
Cloud Run — patient-service
Cloud Run — appointment-service
    │
    ▼ (private IP, VPC only)
Cloud SQL — healthcare-db-dev
```

**Cloud SQL Auth Proxy** is used for local development and CI/CD
integration tests to connect to Cloud SQL without exposing a public IP.

---

## 5. Terraform Structure

```
healthcare-infra/
├── terraform/
│   ├── supabase/          # Old — keep for reference, not used
│   └── gcp/
│       ├── main.tf        # Provider config, project settings
│       ├── variables.tf   # All input variables
│       ├── outputs.tf     # Output values (Cloud SQL IP, Run URLs)
│       ├── vpc.tf         # VPC, subnet, private Google access
│       ├── cloud_sql.tf   # PostgreSQL instance + database + user
│       ├── secret_manager.tf  # All secrets (values set manually)
│       ├── artifact_registry.tf  # Docker image registry
│       ├── cloud_run.tf   # All Cloud Run service deployments
│       ├── cloud_armor.tf # WAF policy + rules
│       └── iam.tf         # Service accounts + WIF + role bindings
```

### terraform.tfvars (gitignored)

```hcl
project_id      = "your-project-id"
region          = "us-west1"
db_password     = "..."   # set manually, stored in Secret Manager
environment     = "dev"
```

Terraform state stored in GCP Cloud Storage bucket — not in the repo.

```hcl
# main.tf backend config
terraform {
  backend "gcs" {
    bucket = "${project_id}-terraform-state"
    prefix = "dev"
  }
}
```

---
6. Secret Management
Current Approach
Secret containers are created by Terraform. Values are set manually via gcloud:
bashecho -n "value" | gcloud secrets versions add SECRET_NAME \
  --data-file=- \
  --project=PROJECT_ID
Sufficient for current stage — secrets are set once and rarely change.
Why set-secrets Was Excluded From run-terraform.sh
A set-secrets command was considered but excluded because:

Secrets set once at initial setup, not on every apply
Manual gcloud command is simple and explicit for now
Automating adds complexity without current benefit

When to Add It Back

Firebase SA needs to replace current placeholder value
Secret rotation becomes a regular requirement
Dev + prod environments need secret sync

Future Implementation

Local: read from gitignored secrets.env, push to Secret Manager
CI/CD: read from GitHub Secrets, push to Secret Manager
Support --rotate, --new, --all flags
Never echo or log secret values

## 7. Cost Estimate (Dev Environment)

| Service | Config | Est. Monthly Cost |
|---|---|---|
| Cloud SQL | db-f1-micro, 10GB SSD | ~$7 |
| Cloud Run | Scales to zero, low traffic | ~$0–2 |
| Artifact Registry | < 1GB images | ~$0.10 |
| Secret Manager | 4 secrets, low access | ~$0 (free tier) |
| Cloud Armor | 1 policy | ~$5 |
| Cloud Logging | < 50GB | ~$0 (free tier) |
| Cloud Build | Low usage | ~$0 (free tier) |
| **Total** | | **~$12–15/month** |

Covered by GCP free trial credits for the dev environment.
Cloud Armor is optional in early dev — can be added in Phase 2.

---

## 8. Setup Order

Infrastructure must be created in this order due to dependencies:

```
1. Terraform state bucket (manual — one time)
2. VPC + subnet
3. Artifact Registry
4. Cloud SQL (requires VPC)
5. Secret Manager secrets (values set manually after creation)
6. IAM service accounts + WIF
7. Cloud Run services (requires Artifact Registry + secrets)
8. Cloud Armor + Load Balancer (requires Cloud Run gateway URL)
```

---

## 9. Phase Status

| Resource | Status |
|---|---|
| GCP Project | ✅ Done |
| Billing linked | ✅ Done |
| APIs enabled | ✅ Done |
| Terraform state bucket | ✅ Done |
| VPC | ✅ Done |
| Cloud SQL | ✅ Done |
| Secret Manager | ✅ Done |
| Artifact Registry | ✅ Done |
| IAM + WIF | ✅ Done |
| Cloud Run | ✅ Done |
| Cloud Armor | ⏳ Phase 2 |

---

*GCP Infrastructure Design — Healthcare AI Platform*
*Region: us-west1 | Env: dev*