$version: "1.0"

namespace com.healthcare

use smithy.api#error
use smithy.api#httpError
use smithy.api#httpPayload
use smithy.api#httpQuery
use smithy.api#httpHeader
use smithy.api#httpLabel
use smithy.api#httpResponseCode
use smithy.api#required
use smithy.api#range
use smithy.api#pattern
use smithy.api#length
use smithy.api#sensitive
use smithy.api#timestampFormat
use smithy.api#documentation

/// Common error response structure
@error("client")
@httpError(400)
structure BadRequestError {
    @required
    error: String

    @required
    message: String

    details: String
}

@error("client")
@httpError(401)
structure UnauthorizedError {
    @required
    error: String

    @required
    message: String
}

@error("client")
@httpError(403)
structure ForbiddenError {
    @required
    error: String

    @required
    message: String
}

@error("client")
@httpError(404)
structure NotFoundError {
    @required
    error: String

    @required
    message: String
}

@error("server")
@httpError(500)
structure InternalServerError {
    @required
    error: String

    @required
    message: String
}

/// Common success response structure
structure SuccessResponse {
    @required
    success: Boolean

    @required
    message: String
}

/// Health check response
structure HealthCheckResponse {
    @required
    status: String

    @required
    service: String

    @required
    version: String

    timestamp: Timestamp
}

/// Pagination request parameters
structure PaginationRequest {
    @httpQuery("page")
    @range(min: 1)
    page: Integer = 1

    @httpQuery("size")
    @range(min: 1, max: 100)
    size: Integer = 20

    @httpQuery("sort")
    sort: String
}

/// Pagination response metadata
structure PaginationResponse {
    @required
    page: Integer

    @required
    size: Integer

    @required
    totalElements: Long

    @required
    totalPages: Integer

    @required
    hasNext: Boolean

    @required
    hasPrevious: Boolean
}

/// Common address structure
structure Address {
    @length(min: 1, max: 200)
    street: String

    @length(min: 1, max: 100)
    city: String

    @length(min: 1, max: 50)
    state: String

    @pattern("^[0-9]{5}(-[0-9]{4})?$")
    zipCode: String

    @length(min: 1, max: 100)
    country: String
}

/// Common contact information
structure ContactInfo {
    @pattern("^\\+[1-9]\\d{1,14}$")
    phone: String

    @pattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    email: String
}

/// Reusable types with validation patterns

/// Name type for first and last names
@pattern("^[a-zA-Z\\s\\-']+$")
@length(min: 2, max: 100)
string Name

/// Phone number type (international format)
@pattern("^\\+[1-9]\\d{1,14}$")
string Phone

/// Email type
@pattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
string Email

/// UUID type
@pattern("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
string Uuid

/// Patient number type (P followed by 11 digits)
@pattern("^P[0-9]{11}$")
string PatientNumber

/// Provider NPI type (10 digits)
@pattern("^[0-9]{10}$")
string Npi

/// ZIP code type (US format)
@pattern("^[0-9]{5}(-[0-9]{4})?$")
string ZipCode

/// Common audit information (internal use only)
@internal
structure AuditInfo {
    @required
    @timestampFormat("date-time")
    createdAt: Timestamp

    @required
    @timestampFormat("date-time")
    updatedAt: Timestamp

    updatedBy: String
}

/// User roles in the system
@enum([
    {
        value: "PATIENT",
        name: "PATIENT",
        documentation: "Patient user role"
    },
    {
        value: "PROVIDER",
        name: "PROVIDER",
        documentation: "Healthcare provider user role"
    }
])
string UserRole

/// User status
@enum([
    {
        value: "ACTIVE",
        name: "ACTIVE",
        documentation: "Active user account"
    },
    {
        value: "INACTIVE",
        name: "INACTIVE",
        documentation: "Inactive user account"
    },
    {
        value: "SUSPENDED",
        name: "SUSPENDED",
        documentation: "Suspended user account"
    }
])
string UserStatus

/// Gender enumeration
enum Gender {
    MALE
    FEMALE
    OTHER
    UNKNOWN
}

/// Appointment status
enum AppointmentStatus {
    AVAILABLE
    SCHEDULED
    CONFIRMED
    IN_PROGRESS
    COMPLETED
    CANCELLED
    NO_SHOW
}

/// Appointment type
enum AppointmentType {
    REGULAR_CONSULTATION
    FOLLOW_UP
    NEW_PATIENT_INTAKE
    PROCEDURE_CONSULTATION
}

/// Medical record type
enum MedicalRecordType {
    DIAGNOSIS
    TREATMENT
    SUMMARY
    LAB_RESULT
    PRESCRIPTION
    NOTE
    OTHER
}

/// Action type for audit logs
enum ActionType {
    CREATE
    READ
    UPDATE
    DELETE
    LOGIN
    LOGOUT
}

/// Resource type for audit logs
enum ResourceType {
    USER_PROFILE
    PATIENT_PROFILE
    PROVIDER_PROFILE
    APPOINTMENT
    MEDICAL_RECORD
}

/// Outcome for audit logs
enum Outcome {
    SUCCESS
    FAILURE
}
