# Services

Spring Boot microservices for the Healthcare AI platform.

## Structure

```
services/
├── pom.xml                # parent POM
├── dev.sh                 # local build/test/run script
├── shared/                # JPA entities, DAOs, enums (library — not deployable)
├── gateway/               # Spring Cloud Gateway — JWT auth, RBAC, routing
├── auth-service/          # register, login, refresh, logout, JWKS
├── patient-service/       # patient profile, encounters, conditions, allergies
├── provider-service/      # provider profile, patient management, admin import
└── appointment-service/   # booking (code exists, not deployed)
```

## Service Status

| Service | Port | Status | Key Endpoints |
|---------|------|--------|---------------|
| gateway | 8080 | deployed | all `/api/**` routes |
| auth-service | 8082 | deployed | `/api/auth/register/patient`, `/register/provider`, `/login`, `/refresh`, `/logout`, `/jwks` |
| patient-service | 8081 | deployed | `/api/patients/me`, `/encounters`, `/conditions`, `/allergies` |
| provider-service | 8083 | deployed | `/api/provider/me`, `/patients`, `/admin/import/**` |
| appointment-service | — | deferred | `/api/appointments/**` (stub exists) |

## Common Commands

```bash
# Build a single service
./dev.sh auth-service build

# Run unit tests
./dev.sh auth-service test
./dev.sh all test

# Build all
./dev.sh all build

# Run locally (installs shared first)
./dev.sh auth-service run

# Package JAR
./dev.sh auth-service package
```

## Adding a New Service

1. Create `services/<name>/` with `pom.xml` and `Dockerfile`
2. Add entity/DAO to `shared/` if needed
3. Add the service to `docker/docker-compose.yml`
4. Add route to `gateway/src/main/resources/application.yml`
