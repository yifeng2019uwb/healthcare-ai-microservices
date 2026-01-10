$version: "2.0"

namespace com.healthcare

use com.healthcare#Address
use com.healthcare#BadRequestError
use com.healthcare#Email
use com.healthcare#Gender
use com.healthcare#HealthCheckResponse
use com.healthcare#InternalServerError
use com.healthcare#MedicalRecordType
use com.healthcare#Name
use com.healthcare#NotFoundError
use com.healthcare#Npi
use com.healthcare#PaginationResponse
use com.healthcare#Phone
use com.healthcare#SuccessResponse
use com.healthcare#UnauthorizedError
use com.healthcare#UserRole
use com.healthcare#UserStatus
use com.healthcare#Uuid
use com.healthcare#ZipCode

/// Provider Service API Models
/// Create provider account request
structure CreateProviderRequest {
    @required
    externalUserId: Uuid

    @required
    firstName: Name

    @required
    lastName: Name

    phone: Phone

    email: Email

    address: Address

    @required
    @timestampFormat("date-time")
    dateOfBirth: Timestamp

    @required
    gender: Gender
}

/// Update provider profile request
structure UpdateProviderProfileRequest {
    firstName: Name
    lastName: Name
    phone: Phone
    email: Email
    address: Address
}

/// Professional information structure
structure ProfessionalInfo {
    @required
    npi: Npi

    @required
    @length(min: 1, max: 100)
    specialty: String

    @required
    @length(min: 1, max: 200)
    licenseNumber: String

    @required
    @length(min: 1, max: 100)
    licenseState: String

    @required
    @timestampFormat("date-time")
    licenseExpiration: Timestamp

    @length(min: 1, max: 500)
    certifications: StringList

    @length(min: 1, max: 1000)
    education: String

    @length(min: 1, max: 1000)
    experience: String
}

/// Update professional information request
structure UpdateProfessionalInfoRequest {
    @required
    professionalInfo: ProfessionalInfo
}

/// Provider search criteria
structure ProviderSearchRequest {
    @httpQuery("specialty")
    specialty: String

    @httpQuery("city")
    city: String

    @httpQuery("state")
    state: String

    @httpQuery("zipCode")
    zipCode: ZipCode

    @httpQuery("name")
    name: String

    @httpQuery("page")
    @range(min: 1)
    page: Integer = 1

    @httpQuery("size")
    @range(min: 1, max: 50)
    size: Integer = 20
}

/// Medical record content (JSON structure)
document MedicalRecordContent

/// Create medical record request
structure CreateMedicalRecordRequest {
    @required
    patientId: Uuid

    @required
    @length(min: 1, max: 200)
    title: String

    @required
    type: MedicalRecordType

    @required
    content: MedicalRecordContent

    @length(min: 1, max: 1000)
    summary: String

    @length(min: 1, max: 1000)
    notes: String
}

/// Update medical record request
structure UpdateMedicalRecordRequest {
    @httpLabel
    @required
    recordId: Uuid

    @length(min: 1, max: 200)
    title: String

    type: MedicalRecordType

    content: MedicalRecordContent

    @length(min: 1, max: 1000)
    summary: String

    @length(min: 1, max: 1000)
    notes: String
}

/// Provider user profile response
structure ProviderUserProfile {
    externalUserId: Uuid

    firstName: Name

    lastName: Name

    phone: Phone

    email: Email

    address: Address

    @timestampFormat("date-time")
    dateOfBirth: Timestamp

    gender: Gender

    role: UserRole

    status: UserStatus
}

/// Provider profile response
structure ProviderProfile {
    @length(min: 1, max: 100)
    providerNumber: String

    professionalInfo: ProfessionalInfo

    @timestampFormat("date-time")
    createdAt: Timestamp

    @timestampFormat("date-time")
    updatedAt: Timestamp
}

/// Provider search result
structure ProviderSearchResult {
    @required
    id: Uuid

    @required
    firstName: Name

    @required
    lastName: Name

    @required
    @length(min: 1, max: 100)
    specialty: String

    @required
    @length(min: 1, max: 200)
    licenseNumber: String

    @required
    @length(min: 1, max: 100)
    licenseState: String

    address: Address

    phone: Phone

    email: Email
}

/// Provider search response
structure ProviderSearchResponse {
    @required
    providers: ProviderSearchResultList

    @required
    pagination: PaginationResponse
}

/// Medical record response
structure MedicalRecordResponse {
    @required
    id: Uuid

    @required
    patientId: Uuid

    @required
    @length(min: 1, max: 200)
    title: String

    @required
    type: MedicalRecordType

    @required
    content: MedicalRecordContent

    @length(min: 1, max: 1000)
    summary: String

    @length(min: 1, max: 1000)
    notes: String

    @required
    @timestampFormat("date-time")
    createdAt: Timestamp

    @required
    @timestampFormat("date-time")
    updatedAt: Timestamp
}

/// Medical record list response
structure MedicalRecordListResponse {
    @required
    medicalRecords: MedicalRecordResponseList

    @required
    pagination: PaginationResponse
}

/// Provider service operations
@http(method: "POST", uri: "/api/providers", code: 201)
operation CreateProvider {
    input: CreateProviderRequest
    output: SuccessResponse
    errors: [
        BadRequestError
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/providers/profile", code: 200)
@readonly
operation GetProviderProfile {
    output: ProviderUserProfile
    errors: [
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/providers/profile", code: 200)
@idempotent
operation UpdateProviderProfile {
    input: UpdateProviderProfileRequest
    output: ProviderUserProfile
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/providers/professional-info", code: 200)
@idempotent
operation UpdateProfessionalInfo {
    input: UpdateProfessionalInfoRequest
    output: ProviderProfile
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/providers/search", code: 200)
@readonly
operation SearchProviders {
    input: ProviderSearchRequest
    output: ProviderSearchResponse
    errors: [
        BadRequestError
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/providers/{providerId}", code: 200)
@readonly
operation GetProviderDetails {
    input: GetProviderDetailsRequest
    output: ProviderSearchResult
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/api/providers/medical-records", code: 201)
operation CreateMedicalRecord {
    input: CreateMedicalRecordRequest
    output: MedicalRecordResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/providers/medical-records/{recordId}", code: 200)
@idempotent
operation UpdateMedicalRecord {
    input: UpdateMedicalRecordRequest
    output: MedicalRecordResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/providers/medical-records/{recordId}", code: 200)
@readonly
operation GetMedicalRecord {
    input: GetMedicalRecordRequest
    output: MedicalRecordResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/providers/patients/{patientId}/medical-records", code: 200)
@readonly
operation GetPatientMedicalRecords {
    input: GetPatientMedicalRecordsRequest
    output: MedicalRecordListResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/health", code: 200)
@readonly
operation HealthCheck {
    output: HealthCheckResponse
}

/// Request structures for path parameters
structure GetProviderDetailsRequest {
    @httpLabel
    @required
    providerId: Uuid
}

structure GetMedicalRecordRequest {
    @httpLabel
    @required
    recordId: Uuid
}

structure GetPatientMedicalRecordsRequest {
    @httpLabel
    @required
    patientId: Uuid

    @httpQuery("page")
    @range(min: 1)
    page: Integer = 1

    @httpQuery("size")
    @range(min: 1, max: 50)
    size: Integer = 20
}
