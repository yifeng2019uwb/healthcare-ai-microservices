$version: "2.0"

namespace com.healthcare

use com.healthcare#UserRole
use com.healthcare#UserStatus
use com.healthcare#BadRequestError
use com.healthcare#UnauthorizedError
use com.healthcare#ForbiddenError
use com.healthcare#InternalServerError
use com.healthcare#HealthCheckResponse

/// Auth Service API Models

/// JWT validation request
structure ValidateTokenRequest {
    @required
    @length(min: 1, max: 2000)
    token: String
}

/// JWT claims structure
structure JwtClaims {
    @required
    @length(min: 1, max: 200)
    sub: String

    @required
    @length(min: 1, max: 200)
    email: String

    @required
    role: UserRole

    @required
    status: UserStatus

    @timestampFormat("date-time")
    iat: Timestamp

    @timestampFormat("date-time")
    exp: Timestamp

    @length(min: 1, max: 200)
    iss: String

    @length(min: 1, max: 200)
    aud: String
}

/// JWT validation response
structure ValidateTokenResponse {
    @required
    valid: Boolean

    claims: JwtClaims

    @length(min: 1, max: 500)
    error: String
}

/// User context for business services
structure UserContext {
    @required
    userId: String

    @required
    email: String

    @required
    role: UserRole

    @required
    status: UserStatus

    @timestampFormat("date-time")
    tokenIssuedAt: Timestamp

    @timestampFormat("date-time")
    tokenExpiresAt: Timestamp
}

/// Auth service operations
@http(method: "POST", uri: "/api/auth/validate", code: 200)
operation ValidateToken {
    input: ValidateTokenRequest
    output: ValidateTokenResponse
    errors: [BadRequestError, UnauthorizedError, InternalServerError]
}

@http(method: "GET", uri: "/health", code: 200)
operation HealthCheck {
    output: HealthCheckResponse
}
