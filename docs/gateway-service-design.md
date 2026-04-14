# Gateway Service Design

> Version: 3.0 | Status: ✅ Deployed | Last Updated: April 2026

---

## Overview

The Gateway is the single entry point for all external requests.
It handles JWT validation and routes to downstream services.
No business logic lives here.

---

## Responsibilities

- Route all incoming requests to the correct downstream service
- Validate RS256 JWT on every protected request (locally via cached JWKS)
- Check JTI against Redis blacklist on every protected request
- Inject user context headers (`X-User-Id`, `X-User-Role`, `X-Username`) for downstream services
- Does NOT handle registration, login, or any business logic

---

## Dependencies

```xml
spring-boot-starter-webflux          <!-- reactive — Spring Cloud Gateway requires reactive -->
spring-cloud-starter-gateway
spring-boot-starter-data-redis-reactive
spring-boot-starter-actuator
jjwt-api:0.12.6
jjwt-impl:0.12.6
jjwt-jackson:0.12.6
spring-cloud-gcp-starter-secretmanager
```

---

## Port

`8080`

---

## Route Table

| Method | Path | Auth Required | Downstream |
|--------|------|---------------|------------|
| POST | `/api/auth/register/patient` | No | auth-service |
| POST | `/api/auth/register/provider` | No | auth-service |
| POST | `/api/auth/login` | No | auth-service |
| POST | `/api/auth/refresh` | No | auth-service |
| POST | `/api/auth/logout` | Yes | auth-service |
| GET  | `/api/patients/**` | Yes | patient-service |
| PUT  | `/api/patients/**` | Yes | patient-service |
| GET  | `/api/provider/**` | Yes | provider-service |
| POST | `/api/provider/**` | Yes | provider-service |
| GET  | `/api/encounters/**` | Yes | appointment-service |
| GET  | `/api/appointments/**` | Yes | appointment-service |
| POST | `/api/appointments/**` | Yes | appointment-service |
| PUT  | `/api/appointments/**` | Yes | appointment-service |
| GET  | `/actuator/health` | No | gateway (self) |

> `/api/encounters/**` and `/api/appointments/**` both route to the same appointment-service.
> Encounters = clinical history (Phase 1). Appointments = booking/scheduling (Phase 2).
> Following FHIR R4 / Epic conventions — these are distinct resources.

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
    ├── 4. Check JTI in Redis blacklist
    │        → blacklisted → 401
    │        → Redis unavailable → 503 (fail closed)
    │
    ├── 5. Inject headers into forwarded request:
    │        X-User-Id:   {sub}
    │        X-User-Role: {role}
    │        X-Username:  {username}
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

## Error Responses

| Scenario | HTTP Status | Message |
|---|---|---|
| Missing Authorization header | 401 | Unauthorized |
| Invalid or expired JWT | 401 | Unauthorized |
| JTI blacklisted | 401 | Unauthorized |
| Redis unavailable | 503 | Service Unavailable |
| Downstream service unreachable | 502 | Bad Gateway |

Gateway never exposes internal error detail to clients.

---

## Headers Forwarded to Downstream

After successful JWT validation, gateway strips the original `Authorization` header
and injects:

| Header | Value | Source |
|--------|-------|--------|
| `X-User-Id` | users.id (UUID) | JWT `sub` claim |
| `X-User-Role` | PATIENT / PROVIDER / ADMIN | JWT `role` claim |
| `X-Username` | username string | JWT `username` claim |

Downstream services trust these headers — they only accept requests via gateway (VPC-internal).

---


## Infrastructure

| Component | GCP Service |
|---|---|
| Gateway runtime | Cloud Run (Port 8080) |
| JWKS source | auth-service `/.well-known/jwks.json` (VPC-internal) |
| Token blacklist | Cloud Memorystore Redis (shared with auth-service) |
| Secret storage | GCP Secret Manager (Redis password via Workload Identity) |

---

## RBAC — Role-to-Path Enforcement (Planned)

> STATUS: Not yet implemented. Tracked as a security layer item.

The gateway injects `X-User-Role` (PATIENT / PROVIDER / ADMIN) into every forwarded request.
Currently, role enforcement is handled implicitly downstream via DB lookups.

Planned: add explicit role checks in `JwtAuthFilter` before forwarding:

```
/api/patients/**              → PATIENT role only
/api/provider/**              → PROVIDER role only
/api/encounters/me/**         → PATIENT role only
/api/encounters/provider/**   → PROVIDER role only
/api/appointments/**          → PATIENT or PROVIDER (fine-grained per endpoint)
```

Wrong-role requests should return `403 Forbidden` at the gateway, not a `404` from a downstream DB miss.

---

## Future Additions

- Rate limiting (per IP, per user)
- Request/response logging with correlation IDs
- Circuit breaker for downstream services
- Cloud Armor WAF integration
