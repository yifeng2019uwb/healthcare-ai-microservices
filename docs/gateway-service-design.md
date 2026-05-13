# Gateway Service Design

> Version: 4.0 | Status: ✅ Deployed | Last Updated: 2026-05-13

---

## Overview

The Gateway is the single entry point for all external requests.
It handles JWT validation, RBAC enforcement, and routing to downstream services.
No business logic lives here.

---

## Responsibilities

- Route all incoming requests to the correct downstream service
- Validate RS256 JWT on every protected request (locally via cached JWKS)
- Enforce role-to-path access control (RBAC) before forwarding
- Inject user context headers (`X-User-Id`, `X-User-Role`, `X-Username`) for downstream services
- Does NOT handle registration, login, or any business logic

---

## Dependencies

```xml
spring-boot-starter-webflux          <!-- reactive — Spring Cloud Gateway requires reactive -->
spring-cloud-starter-gateway
spring-boot-starter-actuator
jjwt-api:0.12.6
jjwt-impl:0.12.6
jjwt-jackson:0.12.6
spring-cloud-gcp-starter-secretmanager
```

> Redis dependency removed — token blacklist skipped. Focus is RBAC and AI governance.

---

## Port

`8080`

---

## Route Table

| Method | Path | Auth Required | Role | Downstream |
|--------|------|---------------|------|------------|
| POST | `/api/auth/register/patient` | No | Any | auth-service |
| POST | `/api/auth/register/provider` | No | Any | auth-service |
| POST | `/api/auth/login` | No | Any | auth-service |
| POST | `/api/auth/refresh` | No | Any | auth-service |
| POST | `/api/auth/logout` | Yes | Any | auth-service |
| GET  | `/api/patients/**` | Yes | PATIENT | patient-service |
| PUT  | `/api/patients/**` | Yes | PATIENT | patient-service |
| GET  | `/api/provider/**` | Yes | PROVIDER | provider-service |
| POST | `/api/provider/**` | Yes | PROVIDER | provider-service |
| GET  | `/api/admin/**` | Yes | ADMIN | provider-service |
| POST | `/api/admin/**` | Yes | ADMIN | provider-service |
| PUT  | `/api/admin/**` | Yes | ADMIN | provider-service |
| DELETE | `/api/admin/**` | Yes | ADMIN | provider-service |
| GET  | `/api/encounters/**` | Yes | PATIENT or PROVIDER | encounter-service (disabled) |
| GET  | `/actuator/health` | No | Any | gateway (self) |

---

## Request Flow

### Public request (no auth)
```
Client → Gateway → route to downstream → return response
```

### Protected request
```
Client → Gateway
    │
    ├── 1. Extract JWT from Authorization: Bearer header
    │        → missing or malformed → 401
    │
    ├── 2. Extract kid from JWT header
    │        → look up kid in local JWKS cache
    │        → kid miss → re-fetch from auth-service/.well-known/jwks.json
    │                     (rate limited: max once per 5 min)
    │        → still not found → 401
    │
    ├── 3. Validate RS256 signature locally
    │        → invalid signature → 401
    │        → expired (exp claim) → 401
    │
    ├── 4. Check role against path (RBAC)
    │        → role not allowed for this path → 403
    │
    ├── 5. Inject headers into forwarded request:
    │        X-User-Id:   {sub}
    │        X-User-Role: {role}
    │        X-Username:  {username}
    │        Authorization: Bearer {token}  (preserved for downstream)
    │
    └── 6. Route to downstream service → return response
```

---

## JWKS Cache

```
Startup       → fetch JWKS from auth-service/.well-known/jwks.json → cache in memory
Per request   → look up kid in cache → validate locally (no network call)
kid miss      → re-fetch JWKS, rate limited to max once per 5 min
Cache TTL     → 5 minutes (background refresh)
```

Gateway never calls auth-service per request for validation — all validation is local.

---

## RBAC — Role-to-Path Enforcement

Enforced in `JwtAuthFilter` at step 4, after JWT validation, before forwarding.

| Path pattern | Allowed role | Downstream |
|---|---|---|
| `/api/patients/**` | PATIENT | patient-service |
| `/api/provider/**` | PROVIDER | provider-service |
| `/api/admin/**` | ADMIN | provider-service |
| `/api/encounters/me/**` | PATIENT | encounter-service |
| `/api/encounters/provider/**` | PROVIDER | encounter-service |
| `/api/auth/**` | Any (public or JWT-only) | auth-service |

Wrong-role requests return `403 Forbidden` at the gateway — never reach the downstream service.

> ADMIN role is isolated to `/api/admin/**` only. Admin cannot access `/api/provider/**` or `/api/patients/**`.

---

## Error Responses

| Scenario | HTTP Status | Message |
|---|---|---|
| Missing Authorization header | 401 | Unauthorized |
| Invalid or expired JWT | 401 | Unauthorized |
| Role not allowed for path | 403 | Forbidden |
| Downstream service unreachable | 502 | Bad Gateway |

Gateway never exposes internal error detail to clients.

---

## Headers Forwarded to Downstream

After successful JWT validation and RBAC check:

| Header | Value | Source |
|--------|-------|--------|
| `X-User-Id` | users.id (UUID) | JWT `sub` claim |
| `X-User-Role` | PATIENT / PROVIDER / ADMIN | JWT `role` claim |
| `X-Username` | username string | JWT `username` claim |
| `Authorization` | Bearer {token} | Preserved from client request |

Downstream services trust these headers — they only accept requests via gateway (VPC-internal).

---

## Infrastructure

| Component | GCP Service |
|---|---|
| Gateway runtime | Cloud Run (Port 8080) |
| JWKS source | auth-service `/.well-known/jwks.json` (VPC-internal) |
| Secret storage | GCP Secret Manager (via Workload Identity) |

---

## Future Additions

- Rate limiting (per IP, per user)
- Request/response logging with correlation IDs
- Circuit breaker for downstream services
- Cloud Armor WAF integration
- Auth/FHIR identity validation (see `docs/auth-fhir-identity-tech-debt.md`)
