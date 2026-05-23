# Database Setup

The platform uses Supabase PostgreSQL as its database. Schema is managed via SQL files in `healthcare-infra/schema/sql/`.

## Schema Deployment

```bash
cd healthcare-infra/schema

# Deploy all tables (idempotent — safe to re-run)
DATABASE_URL="postgresql://postgres:<password>@db.<ref>.supabase.co:5432/postgres" \
  ./run-schema.sh

# Deploy a single table
DATABASE_URL="..." ./run-schema.sh providers
```

Tables are deployed in dependency order: `users` → `organizations` → `patients` → `providers` → `encounters` → `conditions` → `allergies` → `audit_logs` → ...

## Service Configuration

Each service connects to Supabase via environment variables injected by Docker Compose (`docker/docker-compose.yml`):

```
SPRING_DATASOURCE_URL      — jdbc:postgresql://db.<ref>.supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME — postgres
SPRING_DATASOURCE_PASSWORD — <password>
```

For local development (`./dev.sh auth-service run`), set these in the service's `application.yml` or export them before running.

## Schema Design

- **UUIDs**: Assigned by the application layer — not DB-generated (Synthea data has stable UUIDs)
- **Indexes**: All query methods in DAO interfaces must have a corresponding index in the SQL file
- **Audit logs**: Append-only — no DELETE permitted (enforced at DAO layer via `AuditLogDao`)
- **DDL mode**: `validate` in all service configs — Hibernate never modifies the schema

## Enums

All enums are stored as `VARCHAR` in the DB (`@Enumerated(EnumType.STRING)`). Current enums:

| Enum | Values |
|------|--------|
| `UserRole` | PATIENT, PROVIDER, ADMIN |
| `ActionType` | CREATE, READ, UPDATE, DELETE, LOGIN, LOGOUT |
| `Outcome` | SUCCESS, FAILURE |
| `EncounterStatus` | FINISHED, IN_PROGRESS, PLANNED |
| `EncounterType` | AMB, EMER, IMP, ... |
