# Auth Service — Architecture Decision Records (ADR)

> Version: 1.2 | Last Updated: March 2026
> These ADRs document why each security-critical decision was made,
> what alternatives were considered, and what trade-offs were accepted.
>
> **Engineering principle:** Always research how the industry has solved a problem
> before designing a custom solution. Standards exist for good reasons —
> they are battle-tested, interoperable, and understood by other engineers.

---

## Evaluation Principle: GCP-Native First

This project runs entirely on GCP (Cloud Run, Cloud SQL, Secret Manager, VPC).
When evaluating any infrastructure option, GCP-native services are always
evaluated first before considering third-party or cross-cloud alternatives.

Reasons:
- **Integration**: Native IAM, VPC, Workload Identity — no extra auth plumbing
- **Operations**: Single console, single billing account, unified audit logs
- **Security**: No cross-cloud credential management or network exposure
- **Cost**: No egress charges between GCP services in the same region
- **Consistency**: Terraform GCP provider covers everything in one place

Third-party or self-hosted options are only chosen when the GCP-native equivalent
is absent, significantly more expensive, or technically inferior for the use case.

---

## ADR-001: JWT Signing Algorithm — RS256 vs HS256 vs Managed Auth (Auth0/Firebase)

### Context

The auth-service needs to issue JWTs that other services (gateway, patient-service,
provider-service) can verify. The choice of signing algorithm directly affects the
security boundary between services and operational complexity.

### Quick Comparison

| Option | Type | Key Distribution | Token Forgery Risk | Cost | Complexity |
|---|---|---|---|---|---|
| HS256 (symmetric) | Shared secret | Every service needs the secret | High — any service can forge tokens | Free | Low |
| **RS256 (asymmetric)** | Public/private key pair | Only public key shared | Low — only auth-service holds private key | Free | Medium |
| Auth0 | Managed OIDC/OAuth2 | Handled by provider | Very low | $0–$240+/mo | Low (setup) |
| Firebase Auth | Managed OIDC | Handled by provider | Very low | $0–$0.0055/MAU | Low (setup) |

### Options Evaluated

**Option A: HS256 (symmetric HMAC)**
- Pros: Simple to implement, fast verification, widely supported
- Cons: Every service that validates tokens must hold the secret. If patient-service
  is compromised, attacker has the signing key and can forge tokens for any user.
  Violates microservice security boundary — secret sprawl across services.
- Cost: Free

**Option B: RS256 (asymmetric RSA)**
- Pros: Private key never leaves auth-service. All other services only need the
  public key to verify — they cannot forge tokens even if compromised. Industry
  standard for microservices (used by Google, GitHub, Auth0 under the hood).
  Public key can be freely distributed via `/api/auth/public-key` endpoint.
- Cons: Slightly more complex to implement. Key rotation requires coordination.
  RSA operations are slower than HMAC (negligible at this scale).
- Cost: Free

**Option C: Auth0**
- Pros: Fully managed, handles registration/login UI, MFA, social login,
  compliance (SOC2, HIPAA BAA available). No implementation effort for auth flows.
- Cons: Vendor lock-in. At scale, costs escalate ($240+/mo for B2C plan).
  Less control over registration flow — our MRN-matching logic would require
  custom Actions/Rules. Overkill for a controlled internal healthcare system
  where all users are pre-vetted (patients via MRN, providers via provider_code).
- Cost: Free tier (7,000 MAU), then $240+/mo (B2C), $800+/mo (B2B)

**Option D: Firebase Authentication**
- Pros: Fully managed, generous free tier, easy SDK integration, Google-backed.
- Cons: Vendor lock-in to Firebase ecosystem. Custom registration flow (MRN
  matching) requires Cloud Functions workarounds. We already decided to replace
  Firebase Auth in v1.0 of this project due to loss of control over registration
  logic and difficulty enforcing business rules pre-account creation.
  Also creates a second GCP product dependency with its own SDK and billing.
- Cost: Free tier (10,000 verifications/mo), then $0.0055/MAU

### Decision: RS256

RS256 provides the right security boundary for a microservices architecture at
zero cost. The private key is stored in GCP Secret Manager and never leaves
auth-service. All other services verify tokens using only the public key — they
cannot issue tokens even if compromised. This is the same approach used by
Auth0, Okta, and Google internally.

Managed auth providers (Auth0, Firebase) were rejected because our registration
flow requires pre-validation against existing patient/provider records (MRN
matching), which cannot be enforced cleanly at the auth provider boundary without
significant workaround complexity. Building auth in-house on RS256 gives full
control at no additional cost.

### Consequences

- Private key stored in GCP Secret Manager (`jwt-private-key`)
- Public key served via industry standard JWKS endpoint (`/.well-known/jwks.json`, RFC 7517)
- JWT header includes `kid` claim — gateway uses it to look up correct key in JWKS cache
- Gateway caches JWKS with TTL (5min) + re-fetches on `kid` miss (rate limited)
- Key rotation is zero-downtime — publish both old and new key in JWKS during transition
- Old key removed only after all tokens signed with it have expired (max 1hr)
- Same pattern used by Auth0, Okta, Google — industry proven, no custom solution needed

---

## ADR-002: Token Revocation — Redis Blacklist vs Short Expiry Only vs DB Blacklist

### Context

JWT is stateless by design — once issued, a token is valid until expiry.
This creates a security gap: a user who logs out (or whose account is
compromised) still has a valid token until it expires. We need a revocation
mechanism that closes this gap without sacrificing performance.

### Quick Comparison

| Option | Logout Security | Performance | Infrastructure | Cost | Complexity |
|---|---|---|---|---|---|
| Short expiry only (no revocation) | Poor — token valid until exp | Excellent — no lookup | None | Free | None |
| **Redis blacklist + 1hr refresh + 8hr cap** | Strong — immediate revocation + bounded session | Excellent — sub-ms lookup | Redis instance required | ~$35/mo (Memorystore) | Medium |
| DB blacklist (PostgreSQL) | Strong — immediate revocation | Poor — DB hit every request | Existing DB | Free | Low |
| Refresh token rotation only (no blacklist) | Medium — limits refresh reuse | Good | None extra | Free | Medium |

### Options Evaluated

**Option A: Short expiry only (15 min access, no revocation)**
- Pros: Zero infrastructure, zero complexity. Stateless by design.
- Cons: A user logging out from a library computer still has a valid token
  for up to 15 minutes. For a healthcare system with PHI access, this is
  unacceptable. An attacker who steals a token has a guaranteed 15-minute
  window regardless of logout. A long-lived refresh token (7 days) completely
  undermines the 15min access token — stolen refresh token = 7-day access window,
  making the short access token expiry meaningless. Banking apps do not use
  7-day sessions — they expire on browser close or after minutes of inactivity.
- Cost: Free

**Option B: Redis blacklist**
- Pros: Immediate token revocation on logout. Sub-millisecond lookup (in-memory).
  TTL-based auto-expiry means no cleanup jobs — Redis automatically removes
  blacklist entries when the token would have expired anyway. Scales horizontally.
  Redis also serves as foundation for future rate limiting, session caching,
  and distributed locking — investment pays dividends beyond just auth.
- Cons: Requires Redis instance. Adds network hop on every token validation.
  Redis availability becomes part of auth critical path (mitigated with
  connection pooling and fail-open policy for non-sensitive reads if needed).
- Cost: ~$35/mo (GCP Cloud Memorystore Basic tier, 1GB)

**Option C: DB blacklist (PostgreSQL)**
- Pros: No new infrastructure — uses existing Cloud SQL. Simple to implement.
- Cons: Every token validation hits the database. At scale this creates significant
  DB load for what should be a lightweight operation. PostgreSQL is not optimized
  for high-frequency key-value lookups. Adds latency to every authenticated request.
- Cost: Free (uses existing Cloud SQL)

**Option D: Refresh token rotation only**
- Pros: No new infrastructure. Limits refresh token reuse.
- Cons: Access token still valid until expiry after logout. Does not solve the
  library computer scenario. Only protects against refresh token theft, not
  access token theft.
- Cost: Free

### Decision: GCP Cloud Memorystore Basic (Redis) + 1hr Refresh Token + 8hr Absolute Session Cap

**GCP-native evaluation:** GCP Cloud Memorystore is the native managed Redis
service — fully integrated with VPC, IAM, and Cloud Monitoring. No cross-cloud
credential management, no egress charges from Cloud Run in the same region,
Terraform-provisioned in one resource block. This is the correct first choice
for a GCP-native stack.

Redis provides immediate revocation with sub-millisecond performance. Cloud
Memorystore Basic 1GB costs ~$11-12/mo — within the $300 GCP credit budget
for this 90-day portfolio project (~$22-24 total over the project lifetime).
The managed service eliminates ops burden and demonstrates production-grade
infrastructure thinking to portfolio reviewers.

Self-hosted Redis on the existing VM was considered but rejected: it adds
manual configuration complexity, shares RAM with unrelated workloads (佳佳),
and provides no portfolio benefit over a Terraform-managed Cloud Memorystore
instance. The project is torn down after 90 days, so operational simplicity
outweighs the marginal cost saving.

Refresh token expiry is 1 hour — not 7 days. A long-lived refresh token
completely undermines the 15-minute access token: a stolen refresh token
gives an attacker a multi-day window to keep generating fresh access tokens,
making the short access token expiry meaningless. Banking-level systems
expire sessions on browser close or after minutes of inactivity. 1 hour
matches this standard for a healthcare system handling PHI.

An 8-hour absolute session cap (enforced via `original_iat` claim carried
forward through every refresh) prevents indefinite sessions via constant
token refresh activity. Users must re-authenticate once per working day.

### Consequences

- GCP Cloud Memorystore (Redis) required — Terraform config needed
- Redis AUTH password stored in GCP Secret Manager
- Blacklist key pattern: `blacklist:{jti}`, TTL = remaining token lifetime
- Both access token and refresh token jti blacklisted on logout
- Refresh token rotation — old jti blacklisted immediately on every refresh
- `jti` (UUID) claim added to all JWTs
- `original_iat` claim added to refresh tokens — enforces 8hr absolute cap
- Every call to `/internal/auth/validate` checks Redis blacklist
- Every call to `/api/auth/refresh` checks blacklist + absolute session cap
- Redis becomes part of auth critical path — connection pooling required
- Users must re-login after 8 hours regardless of activity

---

## ADR-003: Secret Storage — GCP Secret Manager vs Environment Variables vs Vault vs Hardcoded

### Context

Auth-service requires secure storage for the RS256 private key, RS256 public key,
and Redis AUTH password. The storage mechanism must support rotation, audit
logging, and access control — especially critical for a healthcare system.

### Quick Comparison

| Option | Rotation Support | Audit Log | Access Control | Secret Sprawl Risk | Cost | Complexity |
|---|---|---|---|---|---|---|
| Hardcoded in config | None | None | None | Very High | Free | None |
| Environment variables | Manual redeploy | None | OS-level only | High | Free | Low |
| **GCP Secret Manager** | Versioned + scheduled | Full audit trail | IAM per secret | Low | ~$0.06/10K ops | Low–Medium |
| HashiCorp Vault | Dynamic + fine-grained | Full audit trail | Policy-based | Very Low | $0 (OSS) / $0.03+/hr (HCP) | High |

### Options Evaluated

**Option A: Hardcoded in application.yml or source code**
- Pros: Zero setup. Immediately obvious to developers.
- Cons: Secrets committed to git history. Anyone with repo access has the keys.
  No rotation possible without code change and redeploy. Absolute worst practice
  for any production system, especially healthcare with PHI.
- Cost: Free
- Verdict: Rejected. Never acceptable in any environment beyond local dev scratch.

**Option B: Environment variables (Cloud Run env vars)**
- Pros: Simple, widely understood, no extra infrastructure. Works with Cloud Run
  natively. Slightly better than hardcoded — not in source code.
- Cons: Secrets visible in Cloud Run console to anyone with Cloud Run Viewer role.
  No rotation without redeploy. No audit trail of who read the secret. Secret
  value stored in Cloud Run service configuration — appears in deployment history.
  No versioning.
- Cost: Free

**Option C: GCP Secret Manager**
- Pros: Secrets never appear in source code, config files, or Cloud Run console.
  Full IAM access control per secret — only auth-service Cloud Run service account
  can read `jwt-private-key`. Full audit trail via Cloud Audit Logs (who accessed,
  when). Versioned — old versions retained during rotation. Scheduled rotation
  support. Native Spring Cloud GCP integration via `${sm://...}` references.
  Workload Identity Federation means no JSON key files — service account identity
  is automatic for Cloud Run.
- Cons: Secrets fetched at startup — service restart required for rotation to take
  effect unless using runtime Secret Manager API calls. Small cost per operation.
- Cost: ~$0.06 per 10,000 access operations + $0.06/secret/month. Negligible.

**Option D: HashiCorp Vault**
- Pros: Most powerful option. Dynamic secrets (generate credentials on demand,
  auto-expire). Fine-grained policies. Works across any cloud or on-premise.
  Industry standard for enterprise secret management.
- Cons: Requires running and maintaining a Vault cluster (or paying for HCP Vault).
  Significant operational overhead for a project already running on GCP. Overkill
  when Secret Manager already provides versioning, IAM, and audit logging natively
  on GCP. HCP Vault costs $0.03+/hr (~$22+/mo) minimum.
- Cost: Free (self-hosted OSS) or $22+/mo (HCP Vault Starter)

### Decision: GCP Secret Manager + Workload Identity Federation

Secret Manager is the natural choice for a GCP-native stack. It provides
versioning, IAM-based access control, and full audit logging at negligible cost.
Combined with Workload Identity Federation (no JSON key files), auth-service
accesses secrets via its Cloud Run service account identity automatically —
no credentials needed to access credentials.

Vault was rejected as operational overkill for a GCP-native project. Environment
variables were rejected due to secret visibility in Cloud Run console and lack
of audit trail.

### Consequences

- Secrets: `jwt-private-key`, `jwt-public-key`, `redis-auth-password` in Secret Manager
- Cloud Run service account bound to Secret Manager Secret Accessor role via IAM
- Spring Cloud GCP dependency required for `${sm://...}` property resolution
- Rotation procedure: add new secret version → restart Cloud Run service →
  old tokens expire naturally within 15 minutes (access token lifetime)
- All secret access logged in Cloud Audit Logs

---

## ADR-004: Password Hashing — BCrypt vs Argon2 vs PBKDF2

### Context

User passwords must be hashed before storage. The hashing algorithm must be
slow enough to resist brute-force attacks but fast enough for acceptable login
performance. This is especially important for a healthcare system where
credential compromise could expose PHI.

### Quick Comparison

| Option | Memory-Hard | GPU Resistance | Spring Support | OWASP Recommended | Tunable | Complexity |
|---|---|---|---|---|---|---|
| MD5 / SHA-1 | No | No | No | No (deprecated) | No | None |
| PBKDF2 | No | Poor | Yes | Yes (legacy) | Iterations only | Low |
| **BCrypt** | No | Good | Native | Yes | Cost factor | Low |
| Argon2id | Yes | Excellent | Yes (5.x) | Yes (preferred) | Memory + iterations + parallelism | Medium |

### Options Evaluated

**Option A: MD5 / SHA-256 (unsalted or salted)**
- Verdict: Rejected immediately. Fast hashing algorithms are inappropriate for
  passwords. GPU clusters can compute billions of MD5/SHA hashes per second.
  Not considered further.

**Option B: PBKDF2**
- Pros: NIST-approved, FIPS-compliant, widely supported. Good for environments
  requiring FIPS compliance (federal systems).
- Cons: Not memory-hard — GPU and ASIC attacks remain feasible with enough
  hardware. Weaker than BCrypt or Argon2 against modern attack hardware.
  OWASP now recommends Argon2id over PBKDF2.
- Cost: Free

**Option C: BCrypt**
- Pros: Battle-tested since 1999. Native Spring Security support via
  `BCryptPasswordEncoder`. Automatically salted — no salt management needed.
  Cost factor (strength) is tunable — higher = slower = more brute-force resistant.
  Strength 12 requires ~250ms per hash — acceptable for login, painful for
  attackers. Widely understood and audited.
- Cons: Not memory-hard — theoretically vulnerable to GPU-optimized attacks
  compared to Argon2. 72-character password limit (rarely a real concern).
  Output length fixed at 60 chars.
- Cost: Free

**Option D: Argon2id**
- Pros: Winner of the Password Hashing Competition (2015). Memory-hard — requires
  significant RAM per hash attempt, making GPU/ASIC attacks extremely expensive.
  OWASP's current top recommendation. Tunable across memory, iterations, and
  parallelism. Resistant to side-channel attacks (Argon2id variant).
- Cons: Requires Spring Security 5.x+ for native support (we have 6.x — supported).
  Less battle-tested than BCrypt in production. Slightly more complex configuration
  (memory, iterations, parallelism parameters).
- Cost: Free

### Decision: BCrypt (strength 12)

BCrypt is chosen for its native Spring Security integration, battle-tested
stability, and sufficient security for this use case. Strength 12 provides
~250ms hash time — acceptable UX for login while making brute-force attacks
computationally expensive.

Argon2id is technically superior and would be the choice for a greenfield
system targeting maximum security. BCrypt is chosen here because it is the
Spring Security default, extremely well-audited, and the marginal security
improvement of Argon2id does not justify the additional configuration complexity
for a portfolio system. In a production healthcare system handling millions of
users, Argon2id would be the recommended upgrade path.

PBKDF2 was rejected as it is weaker than BCrypt against GPU attacks and offers
no implementation advantage in a Spring Security context.

### Consequences

- `BCryptPasswordEncoder` registered as Spring bean with strength 12
- ~250ms per password hash/verify operation (acceptable, login is not high-frequency)
- Password hashes stored as 60-char BCrypt strings in `users.password_hash`
- No additional salt management — BCrypt handles salting internally
- Future upgrade path: migrate to Argon2id via Spring Security's
  `DelegatingPasswordEncoder` with BCrypt as legacy fallback

---

## ADR-005: Exception Design — Client-Facing vs Internal Error Detail

### Context

Services need to handle errors at multiple layers (JWT validation, DB calls,
business logic) with precision internally, while never leaking implementation
details to clients.

### Options Evaluated

**Option A: Separate exception class per error case**
- `TokenExpiredException`, `TokenBlacklistedException`, `InvalidCredentialsException` etc.
- Pros: Very explicit, easy to catch specific cases
- Cons: Many classes, handler needs mapping for each, HTTP status scattered across codebase

**Option B: HTTP exceptions (Spring's `ResponseStatusException`)**
- Throw `ResponseStatusException(HttpStatus.UNAUTHORIZED, "...")`  directly
- Pros: No custom classes needed
- Cons: HTTP concerns leak into service layer, internal detail exposed in message,
  no internal error code for logging, not suitable for audit logging

**Option C: Single `AuthServiceException` with internal errorCode (chosen)**
- One exception class carries `HttpStatus` + internal `errorCode` + message
- Handler maps to generic client response, logs full internal detail
- Pros: Clean service layer, precise internal logging, no detail leaked to client,
  one handler method covers all auth errors, adding new error = zero handler changes
- Cons: Less explicit than one class per error — mitigated by errorCode constant

### Decision: Option C

One `AuthServiceException` with internal `errorCode`. Client always receives
a generic HTTP status + standard message. Internal errorCode goes to logs and
`audit_logs` table only. This follows the principle that clients need to know
**what to do** (retry, go to login, show error) not **why it failed internally**.

`HealthcareException` deleted — it carried no information beyond `InternalException`
and created ambiguity about when to use it vs `InternalException`.

Shared exceptions (`ValidationException`, `ConflictException`,
`ResourceNotFoundException`) expose user-facing messages safely — these describe
input errors the client caused and needs to correct, not internal system state.

### Consequences

- `HealthcareException` removed from shared module
- `InternalException` becomes the base for all shared exceptions
- `AuthServiceException` is auth-service's only custom exception
- `AuthExceptionHandler` has one method per exception type, all returning generic messages
- Full error detail always goes to logs — never to client response body

---



| Factor | Value |
|---|---|
| Timeline | 90 days (torn down after demo) |
| GCP credit budget | $300 |
| Estimated monthly burn | ~$48-50/mo (Cloud Run + Cloud SQL + Memorystore + Secret Manager) |
| Total estimated spend | ~$145-150 over 90 days |
| Credit remaining for buffer | ~$150 |
| Infrastructure principle | GCP-native first |

---

## Decision Summary

| Decision | Chosen | Runner-up | Key Reason |
|---|---|---|---|
| JWT algorithm | RS256 + JWKS (RFC 7517) | HS256 | Industry standard — private key never leaves auth-service, zero-downtime rotation via kid-based key lookup |
| Token revocation | GCP Memorystore (Redis) + 1hr refresh + 8hr cap | Self-hosted Redis on VM | GCP-native, Terraform-managed, ~$22 total over 90 days |
| Secret storage | GCP Secret Manager | Environment variables | Audit trail + rotation + Workload Identity |
| Password hashing | BCrypt (strength 12) | Argon2id | Native Spring Security support + battle-tested |
| Exception design | Single `AuthServiceException` + internal errorCode | One class per error | Clean service layer, no detail leaked, precise internal logging |