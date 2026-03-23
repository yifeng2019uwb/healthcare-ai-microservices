# CI/CD Workflows

## Branch Strategy

```
feature/xxx  →  develop  →  main
                  │            │
               auto CI      auto CD
               deploy dev   manual approval
                             → deploy prod
```

| Branch | Purpose | Deploys To |
|---|---|---|
| `feature/*` | Active development | Nowhere |
| `develop` | Integration + testing | Dev (automatic) |
| `main` | Production releases | Prod (manual approval required) |

Feature branches merge to `develop` freely.
`develop` → `main` is a deliberate release decision — done once a week or month when a meaningful feature set is ready.
Even after merging to `main`, prod deploy requires manual approval in GitHub Actions before anything is released.

---

## Workflows

### `ci.yml` — Build, Test, Security Scan
Triggers on every push and PR to `develop` and `main`.

| Step | What it does |
|---|---|
| Validate structure | Checks required dirs and files exist |
| Build | Compiles all services via `dev.sh all build` |
| Unit tests | Runs shared module coverage + service tests |
| OWASP ZAP | Security scan against dev Cloud Run URL |
| Upload artifacts | Test reports + coverage to GitHub Actions |

Authentication: Workload Identity Federation — no credentials stored in GitHub.
See `docs/CICD_SECURITY.md` for full details.

### `cd.yml` — Deploy to Cloud Run
Triggers on merge to `main`. Requires manual approval before deploying to prod.

| Step | What it does |
|---|---|
| Authenticate | WIF keyless auth to prod GCP project |
| Build image | Cloud Build → Artifact Registry |
| Deploy | Cloud Run — gateway, patient-service, appointment-service |
| Smoke test | Hit `/health` endpoints to confirm deployment |

---

## GitHub Secrets Required

| Secret | Used By | What it is |
|---|---|---|
| `WIF_PROVIDER` | Both workflows | Workload Identity Pool provider resource ID |
| `WIF_SERVICE_ACCOUNT` | Both workflows | GCP service account email (masked in logs) |
| `CLOUD_RUN_URL` | `ci.yml` | Dev Cloud Run gateway URL for ZAP scan |

These are identifiers, not credentials. Stored as secrets so they are masked in console output.

---

## Running Locally

Use `scripts/local-ci.sh` to mirror the pipeline locally before pushing.

```bash
# Before every push to develop
./scripts/local-ci.sh --build --test

# Full pipeline (once GCP dev is set up)
./scripts/local-ci.sh --env dev --all --skip-zap
```

See `scripts/README.md` for full usage.