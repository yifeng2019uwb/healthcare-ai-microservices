# Auth Service Design

> Version: 1.1 | Last Updated: March 2026

---

## Overview

Handles user registration, authentication, and JWT token management.
Owns the `users` table — no other service has access to credentials.

All other services validate JWT via the internal validate endpoint.

---

## Responsibilities

- Patient registration — creates account + links to patient record via MRN
- Provider registration — creates account + links to provider record via provider_code
- Login — validate credentials, issue JWT
- Token refresh and logout
- Internal JWT validation for gateway
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
├── password_hash (BCrypt)
├── role (PATIENT, PROVIDER, ADMIN)
├── is_active
├── created_at
└── updated_at
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
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Invalidate token |

### Internal (gateway only, not exposed externally)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/internal/auth/validate` | Validate JWT, return user context |

---

## Request / Response

### POST `/api/auth/register/patient`

Validates MRN + first_name + last_name against patients table.
Creates users row and links auth_id to patient record.

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
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "PATIENT",
  "created_at": "2026-03-26T10:00:00Z"
}
```

Errors:
- `400` — validation error (missing fields)
- `404` — MRN not found in patients table
- `409` — name does not match patient record
- `409` — MRN already linked to another account
- `409` — username or email already exists

---

### POST `/api/auth/register/provider`

Validates provider_code + first_name + last_name against providers table.
Creates users row and links auth_id to provider record.

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
  "id": "uuid",
  "username": "dr_smith",
  "email": "smith@hospital.com",
  "role": "PROVIDER",
  "created_at": "2026-03-26T10:00:00Z"
}
```

Errors:
- `400` — validation error (missing fields)
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
  "expires_in": 3600
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
  "expires_in": 3600
}
```

---

### POST `/internal/auth/validate`

Called by gateway on every request to validate JWT and get user context.

Request:
```json
{
  "token": "eyJhbGci..."
}
```

Response `200`:
```json
{
  "auth_id": "uuid",
  "username": "john_doe",
  "role": "PATIENT",
  "is_active": true
}
```

Errors:
- `401` — invalid or expired token
- `403` — account inactive

---

## Registration Flow

### Patient Registration
```
1. Provider creates patient record in patient-service
   → MRN auto-generated (MRN-000001)
   → Provider gives MRN to patient

2. Patient calls POST /api/auth/register/patient
   → auth-service validates MRN + first_name + last_name
     against patients table (read access via patient_role)
   → Creates users row (role=PATIENT)
   → Links users.id → patients.auth_id
   → Returns JWT
```

### Provider Registration
```
1. Admin creates provider record in provider-service
   → provider_code auto-generated (PRV-000001)
   → Admin gives provider_code to provider

2. Provider calls POST /api/auth/register/provider
   → auth-service validates provider_code + first_name + last_name
     against providers table (read access via auth_role)
   → Creates users row (role=PROVIDER)
   → Links users.id → providers.auth_id
   → Returns JWT
```

---

## JWT Design

```
Header:
  alg: HS256
  typ: JWT

Payload:
  sub: users.id (auth_id)
  username: users.username
  role: PATIENT | PROVIDER | ADMIN
  iat: issued at
  exp: expiry (1 hour for access, 7 days for refresh)
```

JWT signing key stored in GCP Secret Manager (`jwt-secret`).
Never stored in code or config files.

---

## Security

- Passwords hashed with BCrypt (strength 12)
- Access token expiry: 1 hour
- Refresh token expiry: 7 days
- Failed login attempts logged to audit_logs
- All endpoints behind Cloud Armor WAF
- Internal validate endpoint not exposed externally — gateway only
- MRN and provider_code validation prevents unauthorized account creation

---

## Audit Logging

Every auth event written to `audit_logs`:

| Event | Action | Resource Type |
|---|---|---|
| Patient register | CREATE | users |
| Provider register | CREATE | users |
| Login success | READ | users |
| Login failure | READ | users |
| Logout | UPDATE | users |
| Token refresh | READ | users |
| Token validation | READ | users |

---

## Error Response Format

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable message",
  "timestamp": "2026-03-26T10:00:00Z"
}
```

---

## Phase Status

- [ ] Spring Boot project setup
- [ ] User entity + repository
- [ ] Patient register endpoint + MRN validation
- [ ] Provider register endpoint + provider_code validation
- [ ] Login endpoint + JWT generation
- [ ] Token refresh + logout
- [ ] Internal validate endpoint
- [ ] Spring Security filter chain
- [ ] Audit logging integration
- [ ] Unit tests
- [ ] Deploy to Cloud Run