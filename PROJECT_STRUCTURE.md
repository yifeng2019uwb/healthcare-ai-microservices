# Project Structure

## Repository Layout

```
healthcare-ai-microservices/
├── services/                        # Spring Boot microservices
│   ├── pom.xml                      # parent POM (Java 21, Spring Boot 3.4.4)
│   ├── dev.sh                       # build/test/run script
│   ├── shared/                      # JPA entities, DAOs, enums (library — not deployable)
│   ├── gateway/                     # Spring Cloud Gateway — JWT auth, RBAC, routing
│   ├── auth-service/                # register, login, refresh, logout, JWKS
│   ├── patient-service/             # patient profile, encounters, conditions, allergies
│   ├── provider-service/            # provider profile, patient management, admin import
│   ├── ai-service/                  # on-demand clinical summarization + risk analysis (Gemini)
│   └── appointment-service/         # booking stub (not deployed)
│
├── healthcare-infra/                # database and test data
│   ├── schema/
│   │   ├── run-schema.sh            # deploy DDL to Supabase (idempotent)
│   │   └── sql/                     # one SQL file per table
│   │       ├── users.sql
│   │       ├── organizations.sql
│   │       ├── patients.sql
│   │       ├── providers.sql
│   │       ├── encounters.sql
│   │       ├── conditions.sql
│   │       ├── allergies.sql
│   │       └── audit_logs.sql
│   └── synthea/
│       ├── run-synthea.sh           # generate synthetic patient data
│       └── synthea-with-dependencies.jar
│
├── docker/
│   ├── docker-compose.yml           # all services + env var injection
│   └── README.md                    # deploy commands
│
├── integration_tests/               # black-box RestAssured tests against live gateway
│   ├── run-it.sh
│   ├── pom.xml
│   └── src/test/java/
│       ├── util/                    # BaseIT, TestAccounts, LoginHelper, ApiPaths
│       ├── auth/                    # AuthIT, RegisterPatientIT, RegisterProviderIT
│       ├── patient/                 # PatientProfileIT
│       ├── provider/                # ProviderProfileIT
│       ├── ai/                      # AiAnalysisIT (condition write + AI trigger)
│       └── admin/                   # AdminImportIT
│
├── scripts/
│   └── local-ci.sh                  # --build --test stages usable; GCP stages archived
│
├── docs/
│   ├── system-design.md             # architecture, tech stack, security layers
│   ├── database-design.md           # table definitions, indexes, design decisions
│   ├── INTEGRATION_TEST_PLAN.md     # how to run integration tests
│   └── achieve/                     # archived stale docs
│       ├── PROJECT_STRUCTURE.md
│       ├── github-README.md
│       └── RBAC-Practice-Guide.md
│
├── BACKLOG.md                       # tech debt + next-up items
├── DAILY_WORK_LOG.md                # completed tasks log
└── README.md                        # project overview and quick start
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| gateway | 8080 | JWT validation, routing all `/api/**` |
| auth-service | 8082 | register, login, refresh, logout, JWKS |
| patient-service | 8081 | patient profile, encounters, conditions, allergies |
| provider-service | 8083 | provider profile, patient management, admin CSV import |
| ai-service | 8085 | on-demand clinical summarization + risk analysis (Gemini) |
| appointment-service | — | deferred — stub exists, not deployed |

## Key Scripts

```bash
# Build / test services
cd services
./dev.sh auth-service build
./dev.sh all test

# Deploy schema to Supabase
cd healthcare-infra/schema
DATABASE_URL="postgresql://postgres:<password>@db.<ref>.supabase.co:5432/postgres" ./run-schema.sh

# Run integration tests against deployed gateway
cd integration_tests
./run-it.sh all

# Deploy all services via Docker Compose
cd docker
# see docker/README.md
```

## Shared Module

`services/shared/` is a Maven library included by all services. It contains:
- JPA entities: `User`, `Patient`, `Organization`, `Provider`, `Encounter`, `Condition`, `Allergy`, `AuditLog`
- DAO interfaces (Spring Data JPA)
- Enums: `UserRole`, `ActionType`, `Outcome`, `EncounterStatus`, `EncounterType`

Entities are defined once; all services import the library. The shared module itself is
not deployable — it has no `main` class.
