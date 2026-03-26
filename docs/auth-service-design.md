# Auth Service Design

> Version: 1.0 | Last Updated: March 2026

---

## Overview

Handles user registration, authentication, and JWT token management.
Owns the `users` table — no other service has access to credentials.

All other services validate JWT via the internal validate endpoint.

---

## Responsibilities

- User registration (PATIENT, PROVIDER roles)
- Login — validate credentials, issue JWT
- Token refresh and logout
- Internal JWT validation for gateway
- Does NOT handle patient or provider profile data — that belongs to patient-service and provider-service

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
| POST | `/api/auth/register` | Create user account |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/health` | Health check |

### Protected (JWT required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Invalidate token |
| GET | `/api/auth/me` | Get current user info |

### Internal (gateway only, not exposed externally)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/internal/auth/validate` | Validate JWT, return user context |

---

## Request / Response

### POST `/api/auth/register`

Request:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "role": "PATIENT"
}
```

Response `201`:
```json
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "PATIENT",
  "created_at": "2026-03-25T10:00:00Z"
}
```

Errors:
- `409` — username or email already exists
- `400` — validation error

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

### GET `/api/auth/me`

Response `200`:
```json
{
  "id": "uuid",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "PATIENT",
  "is_active": true,
  "created_at": "2026-03-25T10:00:00Z"
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
- Failed login attempts: logged to audit_logs
- All endpoints behind Cloud Armor WAF
- Internal validate endpoint not exposed externally — gateway only

---

## Audit Logging

Every auth event written to `audit_logs`:

| Event | Action | Resource Type |
|---|---|---|
| Register | CREATE | users |
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
  "timestamp": "2026-03-25T10:00:00Z"
}
```

---

## Phase Status

- [ ] Spring Boot project setup
- [ ] User entity + repository
- [ ] Register endpoint
- [ ] Login endpoint + JWT generation
- [ ] Token refresh + logout
- [ ] Internal validate endpoint
- [ ] Spring Security filter chain
- [ ] Audit logging integration
- [ ] Unit tests
- [ ] Deploy to Cloud Run