# Scripts

## dev.sh — Primary Development Script

Located in `services/dev.sh`. All day-to-day build/test/run operations.

```bash
cd services

# Build a single service (installs shared first)
./dev.sh auth-service build
./dev.sh patient-service build

# Run unit tests
./dev.sh shared test
./dev.sh auth-service test
./dev.sh all test

# Build all services
./dev.sh all build

# Run a service locally with Spring Boot
./dev.sh auth-service run

# Package a service into a JAR
./dev.sh auth-service package

# Generate JaCoCo coverage report
./dev.sh auth-service coverage
```

Available services: `shared`, `auth-service`, `gateway`, `patient-service`, `provider-service`, `appointment-service`

---

## local-ci.sh — Build + Test Pipeline

The `--build` and `--test` stages are current and usable. The GCP stages (`--deploy`, `--schema`, `--terraform`, `--data`, `--integration`) reference the old Cloud Run infrastructure and are not used.

```bash
# Before every push — compile + unit tests
./scripts/local-ci.sh --build --test
```

---

## Pre-push Checklist

```bash
# Minimum before every push
cd services && ./dev.sh all test
```
