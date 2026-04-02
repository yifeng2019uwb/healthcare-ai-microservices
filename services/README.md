# Services

Spring Boot microservices for the Healthcare AI platform.

## Structure

```
services/
├── pom.xml            # parent POM
├── dev.sh             # local build/test/run script
├── shared/            # shared JPA entities, DAOs, enums (library, not deployable)
├── gateway/           # Spring Cloud Gateway — JWT auth + routing
├── auth-service/      # register, login, refresh, logout
├── patient-service/   # patient profile, encounters, conditions, allergies
└── provider-service/  # (planned)
```

## Status

| Service | Port (local) | Cloud Run | Endpoints |
|---------|-------------|-----------|-----------|
| gateway | 8080 | ✅ | /api/auth/**, /api/patients/** |
| auth-service | 8082 | ✅ | /api/auth/register, /login, /refresh, /logout |
| patient-service | 8081 | ✅ | /api/patients/me, /encounters, /conditions, /allergies |
| provider-service | 8083 | ⏳ | — |

## Common Commands

```bash
# build/test a single service
./dev.sh patient-service build
./dev.sh patient-service test

# build all
./dev.sh all build

# run locally (installs shared first)
./dev.sh auth-service run
```

## Adding a New Service

1. Create `services/<name>/` with `pom.xml`, `Dockerfile`, `.gcloudignore`
2. Add entity/DAO to `shared/` if needed
3. Add `build_<name>()` and `deploy_<name>()` to `scripts/deploy-services.sh`
4. Add route to `gateway/src/main/resources/application.yml`
