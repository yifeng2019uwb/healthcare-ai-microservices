# Java Spring + RBAC Practice — Plan (aligned with project docs)

This document ties **Java Spring** and **RBAC (Role-Based Access Control)** practice to the project’s **existing plan** so we implement in the right order and don’t code ahead of the plan.

---

## 1. What the project docs already plan

### BACKLOG — Phased strategy
- **Phase 0**: Infrastructure & planning ✅ COMPLETED  
- **Phase 1**: Gateway + Auth + Patient Service 🔄 IN PROGRESS  
- **Phase 2**: Provider Service + basic medical records  
- **Phase 3**: Appointment Service  
- **Phase 4**: AI Service  

RBAC and auth sit in **Phase 1** (Gateway + Auth + Patient).

### BACKLOG — EPIC 2: Authentication & Gateway
- **Goal**: Working authentication with API gateway  
- **Status**: 📋 TO DO  
- **Depends on**: Foundation (shared module, DB, etc.)  

**Planned tasks (order matters):**
| Task        | Description                                              | Deps        |
|------------|----------------------------------------------------------|-------------|
| **AUTH-001** | Implement JWT Context Service (extract user from JWT)   | Foundation  |
| **AUTH-002** | Configure Spring Security + JWT validation             | AUTH-001    |
| **AUTH-003** | User management (registration, login, profile)          | AUTH-002    |
| **AUTH-004** | Enhance Audit Listener with JWT context                 | AUTH-001    |

RBAC (role-based access) is part of **AUTH-002** (Spring Security) and how we use the JWT context in services.

### IMPLEMENTATION_PLAN — Phase 2: Authentication Foundation
- **2.1** JWT Context Service (interface + Spring Security impl, JWT validation)  
- **2.2** Security configuration (Spring Security config, JWT filter, CORS, tests)  
- **2.3** User management (registration, auth, **role management**, profiles)  

**Phase 2 deliverables** (from doc):
- Working JWT authentication  
- User registration and login  
- **Role-based access control**  
- Secure API endpoints  

So **RBAC is already in the plan** under Phase 2 / EPIC 2.

### Design docs — where RBAC is specified
- **authentication-design.md**: “Role-based access control (Patient, Provider)”; roles from JWT; Auth Service extracts role for business services.  
- **gateway-service-design.md**: Gateway validates JWT, extracts role/permissions, allows access to provider-specific endpoints.  
- **patient-service-design.md** / **provider-service-design.md**: Endpoints require JWT with `role: "PATIENT"` or `role: "PROVIDER"`; “Role-Based Access Control” at API level.  
- **database-design.md**: One role per account (PATIENT or PROVIDER); `user_profiles.role`.  

So the **intended RBAC model** is: **PATIENT** and **PROVIDER** from JWT, enforced at API level (and later with Spring Security method security).

---

## 2. Where “Java Spring + RBAC practice” fits

- **Spring**: Used in Gateway, Auth Service, Patient Service (and later Provider, Appointment).  
- **RBAC**: Enforce “who can call what” by role (PATIENT vs PROVIDER) using:  
  - JWT → user + role  
  - Spring Security (config + filter)  
  - Method-level checks (e.g. `@PreAuthorize("hasRole('PATIENT')")`) on controllers/services.  

Practice should follow the **same order** as the plan:

1. **AUTH-001** — JWT Context Service (implement interface; get user/role from JWT; no RBAC yet).  
2. **AUTH-002** — Spring Security config + JWT filter; then add **RBAC** (e.g. require authenticated user + role on `/api/**` and specific endpoints).  
3. **AUTH-003** — Registration/login and profile (can assume roles are set in JWT/database as per design).  

So: **plan first** = do AUTH-001, then AUTH-002 (including RBAC), then AUTH-003. Don’t add RBAC before JWT context and Spring Security are in place.

---

## 3. Suggested practice scope (still plan-only)

Once we implement (in order):

- **JWT Context Service**  
  - Implement `JwtContextService` (e.g. in auth-service or shared) that gets current user id and **role** from JWT (or from SecurityContext after we set it in a filter).  
- **Spring Security + JWT filter**  
  - One service (e.g. patient-service) or gateway: SecurityConfig, JWT filter that validates token and sets `SecurityContext` with user + role (e.g. as `ROLE_PATIENT` / `ROLE_PROVIDER`).  
- **RBAC**  
  - Use `@PreAuthorize("hasRole('PATIENT')")` (or `hasRole('PROVIDER')`) on:  
    - Patient-only endpoints (e.g. create patient, get my profile)  
    - Provider-only endpoints when we add provider-service  
  - Optionally: dev-only headers (e.g. `X-Role`) for local testing **only** if we document and guard by profile.

No code in this guide — only the order and references above. Implementation details (which service gets the filter first, exact package names, etc.) can be decided when we start AUTH-001 / AUTH-002.

---

## 4. Summary

| Topic              | Plan location                          | Order / note                          |
|--------------------|----------------------------------------|---------------------------------------|
| Java Spring        | Used across Gateway, Auth, Patient     | Follow BACKLOG Phase 1 / EPIC 2       |
| RBAC               | Phase 2 deliverables, AUTH-002, design | After JWT context (AUTH-001), with Security config (AUTH-002) |
| Roles              | PATIENT, PROVIDER (UserRole; design)   | From JWT (and DB for profile)         |
| Next step (plan)   | Implement AUTH-001                     | Then AUTH-002 (Spring Security + RBAC) |

---

## 5. References (project docs)

- **BACKLOG.md** — Phases 0–4, EPIC 2 (Auth & Gateway), AUTH-001–004  
- **docs/IMPLEMENTATION_PLAN.md** — Phase 2 (Authentication Foundation), 2.1–2.3  
- **docs/authentication-design.md** — JWT, roles, RBAC scope  
- **docs/gateway-service-design.md** — Gateway auth, role extraction  
- **docs/patient-service-design.md** — Endpoint auth (e.g. JWT + role)  
- **docs/database-design.md** — One role per account, `user_profiles.role`  

When we’re ready to code, we’ll implement in this order and then add concrete steps (files, classes, endpoints) to this guide or to BACKLOG/IMPLEMENTATION_PLAN.
