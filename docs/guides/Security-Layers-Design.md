# Security Layers Design — 8-Layer Model

Design reference for the healthcare-ai-microservices security model. For task tracking and priorities, see [BACKLOG.md](../../BACKLOG.md) Initiative 3.

---

## Prioritized Security Roadmap (implementation order)

| Phase | What to implement | Why |
|-------|-------------------|-----|
| **1 (Now)** | JWT validation at Gateway + basic RBAC roles | Foundation; everything else depends on this |
| **2** | Row-level security (PostgreSQL RLS) + resource ownership | ABAC depth, HIPAA-relevant |
| **3** | Audit logging with AOP | HIPAA relevance, security mindset |
| **4** | Rate limiting + security headers at Gateway | API security |
| **5** | Field-level encryption + Secrets Manager (or Azure Key Vault) | Data security depth |
| **6** | K8s Network Policies + pod security | Infrastructure security |
| **7** | SAST + OWASP Dependency Check in CI | DevSecOps |

---

## Layer 1 — Identity & Authentication

**Difficulty**: Medium. Mostly Auth Service + Gateway filter.

- JWT validation at Gateway (already planned)
- **Token refresh + rotation** — invalidate old tokens after refresh (theft prevention)
- **JWT claims enrichment** — embed roles, user_id, tenant_id in token payload so downstream services don’t re-query DB
- **Token blacklisting** — store revoked JWTs (e.g. Redis) with TTL = remaining token lifetime (logout/account suspension)
- **MFA** — Supabase TOTP; enforce for ADMIN/PHYSICIAN roles if desired
- **Session fingerprinting** — bind JWT to IP + user-agent hash; reject on mismatch (theft detection)

---

## Layer 2 — Authorization & RBAC

**Difficulty**: Medium–High. Shows depth (ABAC, RLS).

- **Basic RBAC**: PATIENT own data only; PHYSICIAN read/write their patients; ADMIN full access
- **ABAC (optional)**: “Physician only records for assigned patients”; “Nurse view vitals, not diagnoses”; custom `PermissionEvaluator` + `@PreAuthorize`
- **Row-Level Security (RLS)**: PostgreSQL/Supabase policies, e.g. `current_setting('app.user_id')`; enforce at DB layer
- **Resource ownership**: every `GET /patients/{id}` (and similar) checks JWT user_id ownership or elevated role

---

## Layer 3 — API Gateway Security

**Difficulty**: Medium. Spring Cloud Gateway + Redis.

- Rate limiting — per-user, per-IP, per-endpoint (e.g. Redis `RequestRateLimiter`)
- Request validation — reject malformed JWT, missing headers, oversized payloads
- IP allowlisting/denylisting
- CORS — whitelist only frontend origins
- Security headers — X-Frame-Options, X-Content-Type-Options, Strict-Transport-Security, Content-Security-Policy
- Request ID propagation — X-Request-ID for tracing and audit
- mTLS between services (internal only) — optional

---

## Layer 4 — Audit Logging & Compliance

**Difficulty**: Medium. HIPAA-relevant. AOP in Spring.

- Audit log: who accessed what PHI, when, from what IP  
  Schema: `user_id`, `role`, `action`, `resource_type`, `resource_id`, `patient_id`, `timestamp`, `ip_address`, `success`
- Immutable audit trail — append-only store (table or S3), no UPDATE/DELETE
- Failed access logging — every 401/403 with context
- Anomaly hook — e.g. 100+ patient records in 5 min → alert
- PHI access justification (break-glass) — optional

---

## Layer 5 — Input Validation & Injection Prevention

**Difficulty**: Low–Medium. Disciplined coding + annotations.

- Bean Validation on all request DTOs (@Valid, @NotNull, @Pattern)
- SQL injection prevention — parameterized queries only
- XSS — sanitize free-text (e.g. clinical notes); HtmlUtils.htmlEscape on output
- Path traversal protection on file paths
- DTOs for API; no entity exposure in request bodies
- Custom validators — ICD-10, LOINC, MRN format where applicable

---

## Layer 6 — Data Security & Encryption

**Difficulty**: Medium.

- Encryption at rest (DB/provider); field-level for SSN, DOB, phone (AES-256) if required
- TLS everywhere; mTLS for internal services
- Pre-signed URLs with short expiry for medical files (no public bucket URLs)
- Secret management — Azure Key Vault / Secrets Manager / Vault (not env vars for prod secrets)
- Data masking in API responses (e.g. SSN masked unless PHI_READ)

---

## Layer 7 — Infrastructure & Container Security

**Difficulty**: Medium. K8s configs.

- K8s Network Policies — only Gateway talks to service pods; restrict pod-to-pod as needed
- Pod Security — non-root, read-only filesystem, drop capabilities
- Secrets via K8s Secrets or external vault
- ServiceAccount per service with minimal RBAC
- Image scanning (e.g. Trivy) in CI before deploy
- Resource limits (CPU/memory)

---

## Layer 8 — Security Testing

**Difficulty**: Low–Medium. Maven + CI.

- SAST — SpotBugs + find-sec-bugs (Maven)
- OWASP Dependency Check in GitHub Actions
- Contract tests — internal calls enforce auth
- Negative tests — access other patient’s data, expired token, SQL injection → expect 403/400

---

## Related docs

- [RBAC Practice Guide](RBAC-Practice-Guide.md)
- [Authentication Design](../../authentication-design.md) (if present)
- [BACKLOG.md](../../BACKLOG.md) — Initiative 3 tasks (SEC-001 … SEC-010)
