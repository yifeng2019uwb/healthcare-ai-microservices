$version: "2.0"

namespace com.healthcare

use com.healthcare#BadRequestError
use com.healthcare#ConflictError
use com.healthcare#Email
use com.healthcare#Gender
use com.healthcare#HealthCheckResponse
use com.healthcare#InternalServerError
use com.healthcare#PersonName
use com.healthcare#Phone

/// Gateway Service API Models for Registration Orchestration
/// Patient registration request (Gateway-specific, includes password)
structure RegisterPatientRequest {
    @required
    email: Email

    @required
    @length(min: 8, max: 100)
    password: String

    @required
    firstName: PersonName

    @required
    lastName: PersonName

    @required
    phone: Phone

    @required
    @timestampFormat("date-time")
    dateOfBirth: Timestamp

    @required
    gender: Gender

    // Optional address fields
    @length(max: 255)
    streetAddress: String

    @length(max: 100)
    city: String

    @length(max: 50)
    state: String

    @length(max: 20)
    postalCode: String

    @length(max: 50)
    country: String
}

/// Patient registration response
structure RegisterPatientResponse {
    success: Boolean

    @length(min: 1, max: 500)
    message: String

    @length(min: 1, max: 255)
    userId: String

    @length(min: 1, max: 50)
    patientNumber: String
}

/// Provider registration request (Gateway-specific, includes password)
structure RegisterProviderRequest {
    @required
    email: Email

    @required
    @length(min: 8, max: 100)
    password: String

    @required
    firstName: PersonName

    @required
    lastName: PersonName

    @required
    phone: Phone

    @required
    @timestampFormat("date-time")
    dateOfBirth: Timestamp

    @required
    gender: Gender

    // Provider-specific fields
    @required
    @length(min: 1, max: 100)
    licenseNumber: String

    @required
    @length(min: 1, max: 100)
    specialization: String

    // Optional address fields
    @length(max: 255)
    streetAddress: String

    @length(max: 100)
    city: String

    @length(max: 50)
    state: String

    @length(max: 20)
    postalCode: String

    @length(max: 50)
    country: String
}

/// Provider registration response
structure RegisterProviderResponse {
    success: Boolean

    @length(min: 1, max: 500)
    message: String

    @length(min: 1, max: 255)
    userId: String

    @length(min: 1, max: 50)
    providerNumber: String
}

/// Gateway service operations
@http(method: "POST", uri: "/api/auth/register/patient", code: 201)
operation RegisterPatient {
    input: RegisterPatientRequest
    output: RegisterPatientResponse
    errors: [
        BadRequestError
        ConflictError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/api/auth/register/provider", code: 201)
operation RegisterProvider {
    input: RegisterProviderRequest
    output: RegisterProviderResponse
    errors: [
        BadRequestError
        ConflictError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/health", code: 200)
@readonly
operation HealthCheck {
    output: HealthCheckResponse
}
