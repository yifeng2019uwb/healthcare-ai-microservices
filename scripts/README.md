# Scripts

## local-ci.sh — Main CI/CD Script

Single script for all pipeline stages. Run individually depending on what
you're working on that day.

```bash
chmod +x scripts/local-ci.sh
./scripts/local-ci.sh          # show help and all options
```

### Stage reference

| Stage | Command | When to use |
|---|---|---|
| Setup | `--setup` | First time — install Java 17 + Maven |
| Build | `--build` | Compile changed |
| Test | `--test` | Unit tests + coverage |
| Terraform | `--terraform` | Infra changed (plan only, safe) |
| Terraform apply | `--terraform --apply` | Ready to apply infra changes |
| Schema | `--schema` | Data model changed |
| Data | `--data` | Generate + reload Synthea data |
| Deploy | `--deploy` | Deploy services to Cloud Run |
| Integration | `--integration` | Test against real Cloud SQL |
| ZAP | `--zap` | Security scan against Cloud Run |
| All | `--all` | Full pipeline before pushing |

### Day-to-day usage

```bash
# Changed Spring Boot code only
./scripts/local-ci.sh --build --test

# Changed Terraform only — check plan, no changes
./scripts/local-ci.sh --terraform

# Changed Terraform + schema
./scripts/local-ci.sh --terraform --apply --schema

# Changed data model — redeploy schema + reload data
./scripts/local-ci.sh --schema --data

# Ready to deploy
./scripts/local-ci.sh --build --test --deploy

# Full pipeline before pushing to main
./scripts/local-ci.sh --all --skip-zap

# Full pipeline including ZAP scan
./scripts/local-ci.sh --all
```

---

## Other scripts

| Script | Purpose |
|---|---|
| `setup-dev.sh` | Install Java + Maven (same as `local-ci.sh --setup`) |
| `debug-ci.sh` | Debug failing CI issues — check structure, Maven, Java |
| `test-ci.sh` | **TODO: delete after verifying `local-ci.sh --build --test` produces same results** |

---

## Pre-push checklist

```bash
# Minimum before every push
./scripts/local-ci.sh --build --test

# Before merging to main
./scripts/local-ci.sh --all --skip-zap
```