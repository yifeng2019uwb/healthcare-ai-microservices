# Auth Service Design

> Version: 1.3 | Last Updated: March 2026

---

## Overview

Handles user registration, authentication, and JWT token management.
Owns the `users` table — no other service has access to credentials.

All other services validate JWT via the internal validate endpoint or
directly using the public key from `/api/auth/public-key`.

---

## Responsibilities

- Patient registration — creates account + links to patient record via MRN
- Provider registration — creates account + links to provider record via provider_code
- Login — validate credentials, issue JWT
- Token refresh and logout (with Redis blacklist)
- Internal JWT validation for gateway
- Public key endpoint for distributed token verification
- Does NOT handle patient or provider profile data — that belongs to
  patient-service and provider-service

---

## Database

Owns: `users` table only
DB user: `auth_service_user` with `auth_role` permissions

```
users
├── id (UUID)
├── username
├── email
├── password_hash (BCrypt strength 12)
├── role (PATIENT, PROVIDER, ADMIN)
├── is_active
├── created_at
└── updated_at
```

---

## Dependencies

```xml
<!-- Spring Boot -->
spring-boot-starter-web
spring-boot-starter-actuator
spring-boot-starter-validation
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-data-redis

<!-- JWT (RS256) -->
jjwt-api:0.11.5
jjwt-impl:0.11.5
jjwt-jackson:0.11.5

<!-- GCP -->
spring-cloud-gcp-starter-secretmanager

<!-- Database -->
postgresql

<!-- Shared module -->
com.healthcare:shared:1.0.0

<!-- Test -->
spring-boot-starter-test
```

---

## API Endpoints

### Public (no auth required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register/patient` | Create patient account + link to patient record |
| POST | `/api/auth/register/provider` | Create provider account + link to provider record |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/health` | Health check |

### Protected (JWT required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/refresh` | Refresh access token using refresh token |
| POST | `/api/auth/logout` | Invalidate token via Redis blacklist |

### Internal (gateway only, not exposed externally)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/.well-known/jwks.json` | JWKS endpoint — returns public key set for gateway token validation (RFC 7517 standard) |

---

## Request / Response

### POST `/api/auth/register/patient`

Validates MRN + first_name + last_name against patients table.
Creates users row, links auth_id to patient record, returns JWT.

Request:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "mrn": "MRN-000001",
  "first_name": "John",
  "last_name": "Doe"
}
```

Response `201`:
```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

Errors:
- `400` — validation error (missing/invalid fields)
- `404` — MRN not found in patients table
- `409` — name does not match patient record
- `409` — MRN already linked to another account
- `409` — username or email already exists

---

### POST `/api/auth/register/provider`

Validates provider_code + first_name + last_name against providers table.
Creates users row, links auth_id to provider record, returns JWT.

Request:
```json
{
  "username": "dr_smith",
  "email": "smith@hospital.com",
  "password": "SecurePass123!",
  "provider_code": "PRV-000001",
  "first_name": "John",
  "last_name": "Smith"
}
```

Response `201`:
```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

Errors:
- `400` — validation error (missing/invalid fields)
- `404` — provider_code not found in providers table
- `409` — name does not match provider record
- `409` — provider_code already linked to another account
- `409` — username or email already exists

---

### POST `/api/auth/login`

Request:
```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

Response `200`:
```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

Errors:
- `401` — invalid credentials
- `403` — account inactive

---

### POST `/api/auth/refresh`

Request:
```json
{
  "refresh_token": "eyJhbGci..."
}
```

Response `200`:
```json
{
  "access_token": "eyJhbGci...",
  "refresh_token": "eyJhbGci...",
  "token_type": "Bearer",
  "expires_in": 900
}
```

Note: refresh token rotation — old refresh token is blacklisted immediately,
new refresh token issued with same original_iat carried forward.

Errors:
- `401` — invalid or expired refresh token
- `401` — refresh token is blacklisted
- `401` — absolute session limit exceeded (8 hours), must login again

---

### POST `/api/auth/logout`

Requires: `Authorization: Bearer <access_token>` header

Request:
```json
{
  "refresh_token": "eyJhbGci..."
}
```

Response `200`:
```json
{
  "message": "Logged out successfully"
}
```

Behavior:
- Extracts jti from access token → adds to Redis blacklist (TTL = remaining lifetime)
- Extracts jti from refresh token → adds to Redis blacklist (TTL = remaining lifetime)

---

### GET `/.well-known/jwks.json`

Industry standard JWKS endpoint (RFC 7517). Gateway fetches this at startup and
caches with TTL. On `kid` miss (key rotation), gateway re-fetches automatically
with rate limiting (max once per 5 minutes) to prevent abuse.

During key rotation, both old and new keys are published simultaneously —
zero downtime. Old key removed only after all tokens signed with it have expired.

Response `200`:
```json
{
  "keys": [
    {
      "kty": "RSA",
      "use": "sig",
      "alg": "RS256",
      "kid": "key-2026-03",
      "n":   "...",
      "e":   "AQAB"
    }
  ]
}
```

Gateway behavior:
```
1. Extract kid from JWT header
2. Look up kid in local JWKS cache
3. Found    → validate signature locally → done
4. Not found → kid miss → re-fetch JWKS (rate limited: max once per 5min)
            → found now → key was rotated → validate → done
            → still not found → invalid token → 401
```

---

## JWT Design

```
Header:
  alg: RS256
  kid: key-2026-03        ← Key ID — tells gateway which key to use for validation
  typ: JWT

Payload:
  sub:          users.id (UUID)
  username:     users.username
  role:         PATIENT | PROVIDER | ADMIN
  jti:          UUID (unique per token, used for blacklisting)
  iat:          issued at (epoch seconds)
  exp:          expiry (epoch seconds)

Access token expiry:          15 minutes
Refresh token expiry:         1 hour
Absolute session cap:         8 hours (from original login, enforced via original_iat claim)

Refresh token additional claims:
  original_iat: original login timestamp (epoch seconds)
                carried forward on every refresh
                session rejected if now - original_iat > 8 hours
```

### JWKS Key Management (RFC 7517)

Industry standard approach — same as Auth0, Okta, Google.

**Normal operation:**
```
JWKS endpoint serves one key:
{ "keys": [ { "kid": "key-2026-03", ... } ] }

Gateway caches JWKS with TTL (5 min)
JWT header contains kid: "key-2026-03"
Gateway looks up kid in cache → validates locally → no auth-service call
```

**Key rotation (zero downtime):**
```
Phase 1: Generate new RSA key pair → add to Secret Manager
Phase 2: Publish BOTH keys in JWKS:
         { "keys": [ { "kid": "key-2026-03" }, { "kid": "key-2026-09" } ] }
Phase 3: Auth-service signs new tokens with new private key (kid: key-2026-09)
Phase 4: Gateway fetches JWKS → kid miss → gets both keys → validates either
Phase 5: Wait for old tokens to expire (max 1hr — refresh token lifetime)
Phase 6: Remove old key from JWKS
```

**Gateway JWKS cache strategy:**
```
Startup          → fetch JWKS → cache in memory (TTL: 5min)
Per request      → lookup kid in cache → validate locally
kid miss         → re-fetch JWKS (rate limited: max once per 5min)
TTL expires      → background refresh
Never            → call auth-service per customer request
```

### Key Storage

| Key | GCP Secret Manager Name | Access |
|---|---|---|
| RS256 private key | `jwt-private-key` | auth-service only |
| RS256 public key | `jwt-public-key` | auth-service (served via JWKS endpoint) |
| Redis AUTH password | `redis-auth-password` | auth-service only |

- Keys accessed via Workload Identity Federation — no JSON key files
- Private key never leaves auth-service
- Public key served in JWKS format at `/.well-known/jwks.json`

---

## Redis Blacklist

```
Provider:    GCP Cloud Memorystore (Redis)
Key pattern: blacklist:{jti}
Value:       "1"
TTL:         remaining token lifetime (exp - now)

On logout:
  → blacklist access token jti  (TTL = remaining access token lifetime)
  → blacklist refresh token jti (TTL = remaining refresh token lifetime)

On gateway (every request):
  → extract kid from JWT header
  → lookup kid in local JWKS cache
  → kid miss → re-fetch JWKS (rate limited) → retry
  → validate JWT signature using matched public key
  → check JWT exp claim — if expired → 401 (no Redis needed)
  → check jti not in Redis blacklist
  → if blacklisted → 401, request rejected at gateway
  → if valid → inject X-User-Id, X-User-Role, X-Username headers → forward

On /api/auth/refresh:
  → validate refresh token signature
  → check refresh token jti not in blacklist
  → check now - original_iat <= 8 hours (absolute session cap)
  → blacklist old refresh token jti immediately (rotation)
  → issue new access token (15min) + new refresh token (1hr)
  → carry forward original_iat into new refresh token
```

---

## Registration Flow

### Patient Registration
```
1. Provider creates patient record in patient-service
   → MRN auto-generated (MRN-000001)
   → Provider gives MRN to patient

2. Patient calls POST /api/auth/register/patient
   → Validate request fields (@Valid)
   → Look up patient by MRN via PatientDao (auth_role read access)
   → Call patient.matchesRegistrationCredentials(mrn, firstName, lastName)
   → Check patient.isRegistered() == false
   → Check username + email not already taken
   → Hash password with BCrypt (strength 12)
   → Save new User (role=PATIENT) via UserDao
   → Call patient.linkAuthAccount(user.getId())
   → Save updated patient via PatientDao
   → Write audit log (CREATE / users / SUCCESS)
   → Issue access + refresh JWT pair
   → Return 201 with tokens
```

### Provider Registration
```
1. Admin creates provider record in provider-service
   → provider_code auto-generated (PRV-000001)
   → Admin gives provider_code to provider

2. Provider calls POST /api/auth/register/provider
   → Validate request fields (@Valid)
   → Look up provider by provider_code via ProviderDao
   → Verify first_name + last_name match provider record
   → Check provider.isRegistered() == false
   → Check username + email not already taken
   → Hash password with BCrypt (strength 12)
   → Save new User (role=PROVIDER) via UserDao
   → Call provider.linkAuthAccount(user.getId())
   → Save updated provider via ProviderDao
   → Write audit log (CREATE / users / SUCCESS)
   → Issue access + refresh JWT pair
   → Return 201 with tokens
```

---

## Class Structure

```
auth-service/src/main/java/com/healthcare/
├── AuthServiceApplication.java
├── config/
│   ├── SecurityConfig.java         — disable filter chain, expose endpoints, BCrypt bean
│   ├── JwtConfig.java              — loads RS256 keys from Secret Manager
│   └── RedisConfig.java            — Redis connection + serialization
├── controller/
│   ├── RegistrationController.java — POST /api/auth/register/patient
│   │                                  POST /api/auth/register/provider
│   ├── AuthController.java         — POST /api/auth/login
│   │                                  POST /api/auth/logout
│   │                                  POST /api/auth/refresh
│   └── TokenController.java        — GET  /.well-known/jwks.json
├── service/
│   ├── AuthService.java            — registration + login + logout logic
│   └── TokenBlacklistService.java  — Redis blacklist operations
├── jwt/
│   └── JwtService.java             — issue + validate + extract claims (RS256)
├── dto/
│   ├── RegisterPatientRequest.java
│   ├── RegisterProviderRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RefreshRequest.java
│   ├── LogoutRequest.java
│   └── JwksResponse.java           — JWKS format response (RFC 7517)
└── exception/
    ├── AuthServiceException.java   — auth-specific base: HttpStatus + internal errorCode
    └── AuthExceptionHandler.java   — @RestControllerAdvice, maps all exceptions to HTTP
```

---

## Exception Design

### Shared exceptions (services/shared)

All shared exceptions extend `InternalException` which carries `HttpStatus` + internal `errorCode`.
The `errorCode` is for internal logging and audit only — never exposed to clients.

```
InternalException              — base, HTTP 500
├── ValidationException        — HTTP 400, errorCode: VALIDATION_ERROR
├── ConflictException          — HTTP 409, errorCode: CONFLICT
└── ResourceNotFoundException  — HTTP 404, errorCode: NOT_FOUND
```

`HealthcareException` is deleted — it added no value over `InternalException`.

### Auth-service exceptions

`AuthServiceException` extends `InternalException`, carries HTTP status + internal error code.
Used for all auth-domain failures. Client never sees the internal error code.

```java
// Thrown internally with precise detail:
throw new AuthServiceException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
    "JWT expired at 2026-03-31T10:15:00Z for user abc-123");

throw new AuthServiceException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
    "Password mismatch for username john_doe");

throw new AuthServiceException(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE",
    "User abc-123 is_active=false");
```

Internal error codes (logged, never sent to client):

| Internal Code | HTTP Status | Meaning |
|---|---|---|
| `INVALID_CREDENTIALS` | 401 | Wrong username or password |
| `INVALID_TOKEN` | 401 | Malformed or unsigned JWT |
| `TOKEN_EXPIRED` | 401 | JWT past expiry |
| `TOKEN_BLACKLISTED` | 401 | jti found in Redis blacklist |
| `SESSION_EXPIRED` | 401 | 8hr absolute cap exceeded |
| `ACCOUNT_INACTIVE` | 403 | User is_active = false |
| `AUTH_INTERNAL_ERROR` | 500 | JWT signing failure, Redis down, etc. |

### Exception handler — `AuthExceptionHandler`

One handler per exception type. Client response is always generic.
Internal detail goes to logs and audit_logs only.

```
AuthServiceException  → log errorCode + message internally → return HTTP status + generic message
InternalException     → log message internally             → return 500 + "An unexpected error occurred"
ValidationException   → return 400 + validation message (safe to expose, user-facing)
ConflictException     → return 409 + conflict message     (safe to expose, user-facing)
ResourceNotFoundException → return 404 + "Not found"      (safe to expose, user-facing)
Exception (catch-all) → log full stack trace              → return 500 + "An unexpected error occurred"
```

---

## Error Response Format

Client always receives a minimal response — no internal detail leaked:

```json
{ "status": 401, "message": "Unauthorized" }
{ "status": 403, "message": "Forbidden" }
{ "status": 500, "message": "An unexpected error occurred" }
```

Validation and conflict errors expose user-facing messages (safe, no internals):
```json
{ "status": 400, "message": "First name is required" }
{ "status": 409, "message": "Username already exists" }
```

Internal logs capture full detail:
```
ERROR Auth error [TOKEN_EXPIRED]: JWT expired at 2026-03-31T10:15:00Z for user abc-123
ERROR Auth error [DB_CONNECTION]: Could not reach Cloud SQL after 3 retries
```

---

## Infrastructure

| Component | GCP Service |
|---|---|
| Auth service runtime | Cloud Run |
| Database | Cloud SQL PostgreSQL 15 |
| Token blacklist | Cloud Memorystore Redis Basic 1GB (~$11-12/mo) |
| Secret storage | Secret Manager |
| Identity | Workload Identity Federation |

---

## Phase Status

### Shared module updates
- [ ] Delete `HealthcareException`
- [ ] Add `HttpStatus` + `errorCode` to `InternalException`
- [ ] Update `ValidationException`, `ConflictException`, `ResourceNotFoundException`
- [ ] Update all shared exception tests

### Auth service
- [ ] Update pom.xml (jpa, security, redis, postgresql, secret manager)
- [ ] Update application.yml (datasource, redis, jwt config)
- [ ] `SecurityConfig` — disable filter chain, BCrypt bean
- [ ] `JwtConfig` — load RS256 keys from Secret Manager
- [ ] `RedisConfig` — Cloud Memorystore connection
- [ ] `JwtService` — RS256 issue + validate + extract claims
- [ ] `TokenBlacklistService` — Redis blacklist operations
- [ ] DTOs — all request/response records
- [ ] `AuthServiceException` + `AuthExceptionHandler`
- [ ] `AuthService` — registration + login + logout logic
- [ ] `RegistrationController`, `AuthController`, `TokenController`
- [ ] Audit logging integration
- [ ] Unit tests

### Infrastructure
- [ ] Cloud Memorystore Terraform config
- [ ] RS256 key pair generation + Secret Manager storage
- [ ] Deploy to Cloud Run