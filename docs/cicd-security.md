# CI/CD Security Guide

> **Why this doc exists:** This repo is public. Standard CI/CD credential practices
> (storing a JSON key in GitHub Secrets) create a long-lived credential that can leak
> in console logs and must be manually rotated. This guide explains the approach we use
> instead — GCP Workload Identity Federation — and how we prevent sensitive data from
> appearing in pipeline output.
>
> Referenced from: `docs/HEALTHCARE_PLATFORM_DESIGN.md` → Section 7 (Security Architecture → CI/CD Security)

---

## All Options — Comparison

Three approaches exist for authenticating GitHub Actions to GCP from a public repo.
We evaluated all three before choosing.

---

### Option A — Service Account JSON Key (GitHub Secret)

Store a GCP service account key file as a GitHub Secret and pass it to the auth action.

```yaml
- uses: google-github-actions/auth@v2
  with:
    credentials_json: ${{ secrets.GCP_SERVICE_ACCOUNT_KEY }}
```

**Pros:**
- Simplest to set up — generate key, paste into GitHub, done
- Works immediately with no GCP-side configuration
- Familiar pattern — most tutorials show this

**Cons:**
- A credential file physically exists (in GitHub's secret store)
- Long-lived — valid until manually rotated, which people forget to do
- If it leaks into a console log (e.g. accidentally echoed), it is immediately usable
- One credential compromises all GCP access granted to that service account
- No automatic expiry — essentially a permanent password
- This is the pattern behind high-profile credential leaks at Uber, Samsung, Toyota

**Verdict:** Acceptable for private repos or quick prototypes. Not appropriate for a
public repo where security is a stated project goal.

---

### Option B — Split Pipeline: Local CI Script + GitHub CI (Lightweight)

Run a full pipeline locally using a shell script that mirrors what GitHub Actions would
do — build, test, terraform, deploy, integration tests. GitHub Actions runs only the
safe subset: compile and unit tests. No GCP credentials ever enter GitHub.

**Local script (`scripts/local-ci.sh`) runs everything:**
```bash
#!/bin/bash
# Full pipeline — runs locally with your GCP credentials from gcloud auth

set -e

echo "=== Build ==="
./mvnw -B package -DskipTests

echo "=== Unit tests ==="
./mvnw -B test

echo "=== Terraform ==="
cd healthcare-infra/terraform/gcp
terraform init
terraform plan
terraform apply -auto-approve
cd ../../..

echo "=== Deploy to Cloud Run ==="
gcloud builds submit --tag gcr.io/$PROJECT_ID/patient-service services/patient-service/
gcloud run deploy patient-service --image gcr.io/$PROJECT_ID/patient-service --region us-west1

echo "=== Integration tests ==="
./mvnw -B verify -Dspring.profiles.active=integration

echo "=== OWASP ZAP scan ==="
docker run -t owasp/zap2docker-stable zap-baseline.py -t https://$SERVICE_URL
```

**GitHub Actions (`ci.yml`) runs only what is safe:**
```yaml
# ci.yml — no GCP auth, no deployment, no integration tests
jobs:
  build-and-test:
    steps:
      - uses: actions/checkout@v4
      - name: Build
        run: ./mvnw -B package -DskipTests
      - name: Unit tests
        run: ./mvnw -B test
      # Terraform, deploy, integration tests, ZAP — all skipped in GitHub
      # Run locally via scripts/local-ci.sh instead
```

**Pros:**
- Zero credential risk in GitHub — nothing stored, nothing can leak from the pipeline
- Full pipeline runs locally including integration tests, terraform, and ZAP scan
- No additional GCP-side configuration required beyond `gcloud auth login`

**Cons:**
- GitHub CI is visibly incomplete — reviewers see a lightweight workflow and may
  think CI/CD coverage is insufficient
- Sensitive values can still print to the local terminal — Terraform output values,
  Spring Boot datasource startup logs, echo statements in scripts
- Deployments only trigger when the script is run manually — no automatic trigger on push
- Local environment differences can cause inconsistent results
- Does not demonstrate automated deployment as a pipeline artifact

**Verdict:** A valid real-world pattern for solo projects on a public repo where the
primary concern is keeping credentials out of GitHub entirely. The tradeoff is that
GitHub CI is visibly incomplete and sensitive values can still appear on the local
terminal. Option C solves the GitHub CI gap while also solving the masking problem
through GitHub Secrets + `add-mask`.

---

### Option C — Workload Identity Federation (Keyless Auth) ✓ CHOSEN

GCP and GitHub establish a trust relationship based on cryptographic certificates.
GitHub Actions proves its identity via a short-lived OIDC token signed by GitHub's CA.
GCP verifies the signature. No key file ever exists.

```yaml
- uses: google-github-actions/auth@v2
  with:
    workload_identity_provider: projects/NUMBER/locations/global/...
    service_account: github-actions@PROJECT.iam.gserviceaccount.com
    # No credentials_json — no secret of any kind
```

**Pros:**
- No credential file exists anywhere — nothing to rotate, nothing to leak
- Token expires automatically after 1 hour maximum
- Restricted to a specific repo by cryptographic claim — forks cannot authenticate
- Audit trail shows exactly which job + commit authenticated
- Sensitive identifiers (service account email, provider ID) stored as GitHub Secrets
  and automatically masked in all console output
- `add-mask` command available to force-mask any dynamically generated value
- Industry best practice — recommended by Google, GitHub, and CISA
- One-time 15-minute setup, then zero maintenance

**Cons:**
- One-time setup requires running ~5 gcloud commands
- Slightly more complex workflow file (provider resource ID is long)
- Requires understanding OIDC concepts to explain in interviews (actually a pro)

**Verdict:** Best choice for a public repo with security as a core project goal.
Eliminates the credential threat entirely rather than managing it.

---

### Final Decision — Option C

| Criterion | Option A | Option B | Option C |
|---|---|---|---|
| No credentials in GitHub pipeline | No | Yes | Yes |
| Full pipeline automated in GitHub | Yes | No (local script only) | Yes |
| Integration tests in CI | Yes | No (local only) | Yes |
| Credential can leak in GitHub logs | Yes | No | No |
| Sensitive values masked in console | Partial | No (local terminal) | Yes |
| Requires manual rotation | Yes | No | No |
| Setup complexity | Low | Low | Low (one-time) |

Option C is the only choice that gives full CI/CD in GitHub, zero credential risk,
and controlled masking of sensitive identifiers in console output.

---

## How Option C Works in Detail

The problem with Option A in concrete terms — storing a GCP service account JSON key
in GitHub Secrets creates a long-lived credential that:

- Lives as a JSON file in GitHub's secret store
- Is valid until manually rotated (people forget)
- Can leak into console output if accidentally echoed
- Gives anyone who obtains it full GCP access until revoked
- Has no automatic expiry — it's essentially a permanent password

Option C replaces all of this with a cryptographic proof-of-identity flow:

---

## How Workload Identity Federation Works Step by Step

```
1. GitHub Actions job starts
        │
        ▼
2. Runner requests an OIDC token from GitHub's identity service
   Token contains signed claims:
     - repository: yifeng2019uwb/healthcare-ai-microservices
     - ref: refs/heads/main
     - job_workflow_ref: .../.github/workflows/ci.yml
        │
        ▼
3. Runner sends this OIDC token to GCP Security Token Service (STS)
        │
        ▼
4. GCP STS verifies:
     ✓ Token signed by GitHub's public certificate? Yes
     ✓ repository claim matches our allow-list? Yes
     ✓ Token not expired? Yes
        │
        ▼
5. GCP STS issues a short-lived GCP access token (max 1 hour)
        │
        ▼
6. Runner uses GCP token for: terraform apply, docker push, gcloud commands
        │
        ▼
7. Job finishes. Token expires automatically. Nothing persists.
```

### What This Means for Security

| Property | GitHub Secret Key | Workload Identity Federation |
|---|---|---|
| Credential exists as file | Yes | No |
| Expiry | Never (until manual rotation) | 1 hour |
| Rotation required | Yes — manually | No — automatic |
| Can leak in logs | Yes | No (token never printed) |
| Scope control | Per key permissions | Per-repo, per-branch |
| Audit trail | Who used the key | Exactly which job + commit |

---

## One-Time Setup (Run Locally, Never Again)

Replace `YOUR_PROJECT_ID` and `YOUR_PROJECT_NUMBER` with your actual GCP values.
Run `gcloud projects describe YOUR_PROJECT_ID` to find the project number.

### Step 1 — Create a service account for GitHub Actions

```bash
gcloud iam service-accounts create github-actions \
  --project=YOUR_PROJECT_ID \
  --display-name="GitHub Actions CI/CD"
```

### Step 2 — Grant it least-privilege roles

Only give it what CI/CD actually needs. Do not use `roles/owner` or `roles/editor`.

```bash
PROJECT=YOUR_PROJECT_ID
SA=github-actions@${PROJECT}.iam.gserviceaccount.com

# Deploy to Cloud Run
gcloud projects add-iam-policy-binding $PROJECT \
  --member="serviceAccount:${SA}" \
  --role="roles/run.admin"

# Push Docker images to Artifact Registry
gcloud projects add-iam-policy-binding $PROJECT \
  --member="serviceAccount:${SA}" \
  --role="roles/artifactregistry.writer"

# Read secrets (for integration tests)
gcloud projects add-iam-policy-binding $PROJECT \
  --member="serviceAccount:${SA}" \
  --role="roles/secretmanager.secretAccessor"

# Allow Cloud Run to act as the service account
gcloud iam service-accounts add-iam-policy-binding $SA \
  --member="serviceAccount:${SA}" \
  --role="roles/iam.serviceAccountUser"
```

### Step 3 — Create the Workload Identity Pool

```bash
gcloud iam workload-identity-pools create "github-pool" \
  --project=YOUR_PROJECT_ID \
  --location="global" \
  --display-name="GitHub Actions Pool"
```

### Step 4 — Create the OIDC provider

The `attribute-condition` restricts which GitHub repos can authenticate.
Only your specific repo can get a GCP token — no other GitHub user or repo.

```bash
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
  --project=YOUR_PROJECT_ID \
  --location="global" \
  --workload-identity-pool="github-pool" \
  --display-name="GitHub OIDC Provider" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository,attribute.ref=assertion.ref" \
  --attribute-condition="assertion.repository=='yifeng2019uwb/healthcare-ai-microservices'"
```

### Step 5 — Allow GitHub to impersonate the service account

```bash
PROJECT_NUMBER=YOUR_PROJECT_NUMBER

gcloud iam service-accounts add-iam-policy-binding \
  github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com \
  --project=YOUR_PROJECT_ID \
  --member="principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-pool/attribute.repository/yifeng2019uwb/healthcare-ai-microservices" \
  --role="roles/iam.workloadIdentityUser"
```

### Step 6 — Note the provider resource name

You need this string in your workflow file. Get it with:

```bash
gcloud iam workload-identity-pools providers describe github-provider \
  --project=YOUR_PROJECT_ID \
  --location=global \
  --workload-identity-pool=github-pool \
  --format="value(name)"

# Output looks like:
# projects/123456789/locations/global/workloadIdentityPools/github-pool/providers/github-provider
```

---

## GitHub Secrets to Configure

Before the workflows run, store these two values in GitHub → Settings → Secrets → Actions.
These are not credentials — they are identifiers. Storing them as secrets causes GitHub
to automatically mask them in all console output.

| Secret name | Value | Why store as secret |
|---|---|---|
| `WIF_PROVIDER` | `projects/NUMBER/locations/global/workloadIdentityPools/github-pool/providers/github-provider` | Masked in logs — not a credential but identifies your IAM setup |
| `WIF_SERVICE_ACCOUNT` | `github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com` | Masked in logs — this is the exact IAM principal, worth hiding |

Neither value is a credential. An attacker who somehow obtained both still cannot
authenticate without a valid GitHub OIDC token from a job running in your specific repo.
Storing them as secrets is an extra layer — it removes the information they would need
to probe your IAM configuration.

---

## GitHub Actions Workflow Files

### `ci.yml` — Build, Test, Security Scan

```yaml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

permissions:
  contents: read
  id-token: write    # Required — allows GitHub to request OIDC token

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      # WIF_PROVIDER and WIF_SERVICE_ACCOUNT stored as GitHub Secrets
      # Both values are automatically masked in all console output
      - name: Authenticate to GCP
        id: auth
        uses: google-github-actions/auth@v2
        with:
          workload_identity_provider: ${{ secrets.WIF_PROVIDER }}
          service_account: ${{ secrets.WIF_SERVICE_ACCOUNT }}

      # Force-mask the project number returned by auth step
      # gcloud commands print it naturally — this suppresses it
      - name: Mask project number
        run: |
          echo "::add-mask::${{ steps.auth.outputs.project_id }}"
          echo "::add-mask::$(gcloud config get-value project)"

      - name: Set up Java 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Build
        run: ./mvnw -B package -DskipTests

      - name: Unit tests
        run: ./mvnw -B test

      - name: Integration tests
        run: ./mvnw -B verify -Dspring.profiles.active=integration
        # DB password read from GCP Secret Manager at Spring Boot startup
        # Connection via Cloud SQL Auth Proxy — no password in URL

      - name: OWASP ZAP baseline scan
        uses: zaproxy/action-baseline@v0.12.0
        with:
          target: 'https://${{ secrets.CLOUD_RUN_URL }}'
          rules_file_name: '.zap/rules.tsv'
          cmd_options: '-a'

      - name: Upload ZAP report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: zap-report
          path: report_html.html
```

### `cd.yml` — Deploy to Cloud Run (main branch only)

```yaml
name: CD

on:
  push:
    branches: [main]

permissions:
  contents: read
  id-token: write

jobs:
  deploy:
    runs-on: ubuntu-latest
    environment: production   # Requires manual approval in GitHub settings

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Authenticate to GCP
        id: auth
        uses: google-github-actions/auth@v2
        with:
          workload_identity_provider: ${{ secrets.WIF_PROVIDER }}
          service_account: ${{ secrets.WIF_SERVICE_ACCOUNT }}

      # Mask project number before any gcloud commands run
      - name: Mask project number
        run: |
          echo "::add-mask::${{ steps.auth.outputs.project_id }}"
          echo "::add-mask::$(gcloud config get-value project)"

      - name: Set up Cloud SDK
        uses: google-github-actions/setup-gcloud@v2

      - name: Build and push Docker image
        run: |
          gcloud builds submit \
            --tag us-west1-docker.pkg.dev/$(gcloud config get-value project)/healthcare/patient-service:${{ github.sha }} \
            services/patient-service/
          # Project ID in the tag is masked in output

      - name: Deploy to Cloud Run
        run: |
          gcloud run deploy patient-service \
            --image us-west1-docker.pkg.dev/$(gcloud config get-value project)/healthcare/patient-service:${{ github.sha }} \
            --region us-west1 \
            --platform managed \
            --no-allow-unauthenticated \
            --set-secrets="DB_PASSWORD=db-password:latest" \
            --set-secrets="FIREBASE_SA=firebase-service-account:latest"
```

---

## Console Log Masking — What GitHub Masks and What It Doesn't

### How GitHub Secrets Masking Works

When a value is stored as a GitHub Secret and referenced via `${{ secrets.X }}`,
GitHub registers that exact string and automatically replaces it with `***` anywhere
it appears in stdout or stderr for that job.

```
# If WIF_SERVICE_ACCOUNT = "github-actions@my-project.iam.gserviceaccount.com"
# And gcloud prints it during auth:

BEFORE:  Authenticating as github-actions@my-project.iam.gserviceaccount.com
AFTER:   Authenticating as ***
```

### What Gets Masked vs What Doesn't

| Scenario | Masked? | Reason |
|---|---|---|
| `${{ secrets.WIF_SERVICE_ACCOUNT }}` used in workflow | Yes | Registered as secret |
| `${{ secrets.WIF_PROVIDER }}` used in workflow | Yes | Registered as secret |
| Service account email printed by gcloud | Yes | Value matches registered secret |
| GCP project ID | No | Not stored as secret |
| Cloud Run service URL | No | Not stored as secret |
| Cloud SQL instance name | No | Not stored as secret |
| Secret Manager secret names | No | Names are not secret values |
| Secret Manager secret values | Never printed | Terraform never outputs them |
| Values added via `add-mask` | Yes | Explicitly registered mid-job |

### The `add-mask` Command

For values that are dynamically generated during the job (like project number returned
by the auth step), use `add-mask` to register them for masking before any commands
that might print them:

```yaml
- name: Authenticate to GCP
  id: auth
  uses: google-github-actions/auth@v2
  with:
    workload_identity_provider: ${{ secrets.WIF_PROVIDER }}
    service_account: ${{ secrets.WIF_SERVICE_ACCOUNT }}

# Must run immediately after auth, before any gcloud commands
- name: Mask dynamic values
  run: |
    echo "::add-mask::${{ steps.auth.outputs.project_id }}"
    echo "::add-mask::$(gcloud config get-value project)"
```

### What the Console Actually Shows

With all masking in place, the CI log looks like:

```
Step: Authenticate to GCP
  workload_identity_provider: ***       ← masked (GitHub Secret)
  service_account: ***                  ← masked (GitHub Secret)
  Successfully authenticated as ***     ← masked (value matches secret)

Step: Mask dynamic values
  ::add-mask::***                       ← project number now registered

Step: Deploy to Cloud Run
  Deploying to project [***]            ← masked by add-mask
  Region: us-west1                      ← not masked, acceptable
  Service: patient-service              ← not masked, acceptable
  Revision: patient-service-00001       ← not masked, acceptable
  URL: https://patient-service-abc-uw.a.run.app  ← not masked, public endpoint
```

Project ID and resource names remain visible. This is acceptable — they are
identifiers, not credentials. The IAM principal (service account email) and
the WIF configuration identifiers are fully masked.

---

## Console Log Leak Prevention

### Common Cases and Fixes

Running `terraform apply` or integration tests can accidentally print sensitive values
to the console. Here is each case and the fix.

**Case 1 — Terraform output values**

```hcl
# BAD — Terraform prints this in plain text
output "db_connection_string" {
  value = "postgresql://${var.db_user}:${var.db_password}@${google_sql_database_instance.main.ip_address}..."
}

# GOOD — marked sensitive, Terraform redacts with (sensitive value)
output "db_connection_string" {
  value     = "postgresql://${var.db_user}:${var.db_password}@${google_sql_database_instance.main.ip_address}..."
  sensitive = true
}
```

**Case 2 — Passing variables on the command line**

```bash
# BAD — password visible in shell history and CI logs
terraform apply -var="db_password=mysecretpassword"

# GOOD — use environment variable prefix TF_VAR_
export TF_VAR_db_password="mysecretpassword"   # sourced from Secret Manager or local .env
terraform apply
# Terraform reads TF_VAR_db_password from environment silently
```

**Case 3 — Echoing secrets in shell scripts**

```bash
# BAD
echo "Connecting with password: $DB_PASSWORD"
psql -h $DB_HOST -U $DB_USER -p $DB_PASSWORD ...

# GOOD — use env vars that psql reads automatically, never echo them
export PGPASSWORD="$DB_PASSWORD"
psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" -c "\dt"
unset PGPASSWORD   # clear after use
```

**Case 4 — Spring Boot logging datasource URL**

Spring Boot sometimes logs the datasource URL on startup including credentials.
Prevent this:

```yaml
# application.yml
logging:
  level:
    com.zaxxer.hikari: WARN      # Suppress HikariCP connection pool logs
    org.springframework.jdbc: WARN
spring:
  datasource:
    url: ${DB_URL}               # From env — not hardcoded
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      connection-init-sql:       # Leave blank — prevents logging init SQL
```

**Case 5 — GitHub Actions accidentally printing secrets**

GitHub automatically masks values registered as secrets. But this only works if you
pass them through `secrets.*` or `env:`. Never construct them inline.

```yaml
# BAD — GitHub cannot mask this because it's constructed inline
- run: echo "DB_URL=postgresql://user:${{ secrets.DB_PASS }}@host/db"

# GOOD — pass as env var, GitHub masks the secret value in logs
- run: ./run-tests.sh
  env:
    DB_PASSWORD: ${{ secrets.DB_PASS }}   # masked as *** in logs
```

---

## What to Commit vs. Gitignore

### `.gitignore` additions for this project

```gitignore
# Terraform — real values never committed
*.tfvars
!*.tfvars.example        # Example templates are safe to commit

# GCP credentials
gcp-credentials.json
service-account*.json
*.json
!package.json
!package-lock.json
!**/test-fixtures/*.json  # Test fixture JSON files are safe

# Environment files
.env
.env.*
!.env.example

# Synthea output — generated data, not committed
data/synthea/output/
output/

# Local secrets
secrets/
config/local/
```

### Safe to commit publicly

```
terraform/gcp/main.tf              ✓  Resource definitions (no real values)
terraform/gcp/variables.tf         ✓  Variable declarations
terraform/gcp/terraform.tfvars.example  ✓  Template with fake values
.github/workflows/ci.yml           ✓  References ${{ secrets.X }}, WIF provider ID
.github/workflows/cd.yml           ✓  Same
docs/DESIGN.md                     ✓  Architecture documentation
docs/CICD_SECURITY.md              ✓  This file
```

### Never commit

```
terraform/gcp/terraform.tfvars     ✗  Real project ID, connection strings
gcp-credentials.json               ✗  Service account key (shouldn't exist at all with WIF)
.env                               ✗  Local dev environment variables
secrets/                           ✗  Any credential files
data/synthea/output/               ✗  Generated patient data (gitignored anyway)
```

---

## Frequently Asked Questions

**Q: The Workload Identity Provider ID is in my public workflow file. Is that a problem?**

No. The provider ID is like a lock's serial number — useless without the key. The "key"
in WIF is GitHub's OIDC token, which can only be obtained by a GitHub Actions runner
actually executing a job. An attacker who sees your provider ID in the workflow file
cannot use it without also controlling a job in your specific repository.

**Q: What if someone forks my repo and tries to authenticate as me?**

The `attribute-condition` we set in Step 4 restricts authentication to
`assertion.repository == 'yifeng2019uwb/healthcare-ai-microservices'` specifically.
A fork at `attacker/healthcare-ai-microservices` has a different repository claim
and is rejected by GCP STS.

**Q: Do I still need GitHub Secrets for anything?**

With full WIF setup, no GCP credentials need to be in GitHub Secrets. The only things
you might still put in GitHub Secrets are non-GCP values like a Slack webhook URL for
notifications, or a third-party API key that doesn't support WIF. For everything GCP,
WIF replaces GitHub Secrets entirely.

**Q: What about Terraform state? That can contain sensitive values.**

Store Terraform state in a private GCP Cloud Storage bucket, not in the repo.

```hcl
# terraform/gcp/main.tf
terraform {
  backend "gcs" {
    bucket = "YOUR_PROJECT_ID-terraform-state"
    prefix = "healthcare"
  }
}
```

The bucket is private. GitHub Actions can access it via the WIF token. State never
touches the repo.

**Q: My integration tests need a real database. How do I connect without printing the password?**

Use Cloud SQL Auth Proxy in the CI job. It creates a local socket connection that
the application treats as `localhost:5432` — no password in the connection string.
The proxy authenticates using the WIF token automatically.

```yaml
- name: Start Cloud SQL Auth Proxy
  run: |
    curl -o cloud-sql-proxy https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.8.0/cloud-sql-proxy.linux.amd64
    chmod +x cloud-sql-proxy
    ./cloud-sql-proxy YOUR_PROJECT_ID:us-west1:healthcare-db &
    sleep 3  # Wait for proxy to be ready

- name: Run integration tests
  run: ./mvnw verify -Dspring.profiles.active=integration
  env:
    SPRING_DATASOURCE_URL: jdbc:postgresql://127.0.0.1:5432/healthcare
    SPRING_DATASOURCE_USERNAME: postgres
    SPRING_DATASOURCE_PASSWORD: ""   # Proxy handles auth — no password needed
```

---

---

*CI/CD Security Guide — Healthcare AI Platform*
*Pattern: GCP Workload Identity Federation + GitHub Actions OIDC*