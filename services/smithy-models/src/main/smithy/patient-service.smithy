$version: "2.0"

namespace com.healthcare

use com.healthcare#Address
use com.healthcare#BadRequestError
use com.healthcare#Email
use com.healthcare#Gender
use com.healthcare#HealthCheckResponse
use com.healthcare#InternalServerError
use com.healthcare#Name
use com.healthcare#NotFoundError
use com.healthcare#PatientNumber
use com.healthcare#Phone
use com.healthcare#UnauthorizedError
use com.healthcare#UserRole
use com.healthcare#UserStatus
use com.healthcare#Uuid

/// Patient Service API Models
/// Create patient account request (INTERNAL - only called by Gateway)
@internal
structure CreatePatientAccountRequest {
    @required
    externalUserId: Uuid

    @required
    firstName: Name

    @required
    lastName: Name

    phone: Phone

    email: Email

    address: Address

    emergencyContactPhone: Phone
}

/// Create patient account response
structure CreatePatientAccountResponse {
    success: Boolean

    @length(min: 1, max: 500)
    message: String
}

/// Update patient profile request
structure UpdatePatientProfileRequest {
    firstName: Name
    lastName: Name
    phone: Phone
    email: Email
    address: Address
    emergencyContactPhone: Phone
}

/// Medical history structure
structure MedicalHistory {
    conditions: StringList
    surgeries: StringList
    medications: StringList
    allergies: StringList
    familyHistory: StringList
}

/// Allergies structure
structure Allergies {
    knownAllergies: StringList
    reactions: StringList
    severity: String
}

/// Insurance information
structure InsuranceInfo {
    provider: String
    policyNumber: String
    groupNumber: String
    effectiveDate: String
    expirationDate: String
}

/// Update patient info request
structure UpdatePatientInfoRequest {
    medicalHistory: MedicalHistory

    allergies: Allergies

    insurance: InsuranceInfo

    @length(min: 1, max: 100)
    primaryCarePhysician: String

    @length(min: 1, max: 500)
    currentMedications: String
}

/// Patient user profile response
structure PatientUserProfile {
    externalUserId: Uuid

    firstName: Name

    lastName: Name

    phone: Phone

    email: Email

    address: Address

    emergencyContactPhone: Phone

    @timestampFormat("date-time")
    dateOfBirth: Timestamp

    gender: Gender

    role: UserRole

    status: UserStatus
}

/// Patient profile response
structure PatientProfile {
    patientNumber: PatientNumber
    medicalHistory: MedicalHistory
    allergies: Allergies
    insurance: InsuranceInfo
    primaryCarePhysician: String
    currentMedications: String
}

/// Medical history response
structure MedicalHistoryResponse {
    appointments: AppointmentSummaryList
    medicalRecords: MedicalRecordSummaryList
}

/// Appointment summary for medical history
structure AppointmentSummary {
    id: Uuid

    @timestampFormat("date-time")
    scheduledTime: Timestamp

    @length(min: 1, max: 100)
    providerName: String

    @length(min: 1, max: 100)
    specialty: String

    status: String

    notes: String
}

/// Medical record summary for medical history
structure MedicalRecordSummary {
    id: Uuid

    @timestampFormat("date-time")
    createdAt: Timestamp

    @length(min: 1, max: 100)
    providerName: String

    @length(min: 1, max: 100)
    type: String

    @length(min: 1, max: 200)
    title: String

    summary: String
}

/// Patient service operations
@http(method: "POST", uri: "/api/patients", code: 201)
operation CreatePatient {
    input: CreatePatientAccountRequest
    output: CreatePatientAccountResponse
    errors: [
        BadRequestError
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/patients/profile", code: 200)
@readonly
operation GetPatientProfile {
    output: PatientUserProfile
    errors: [
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/patients/profile", code: 200)
@idempotent
operation UpdatePatientProfile {
    input: UpdatePatientProfileRequest
    output: PatientUserProfile
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/patients/patient-info", code: 200)
@idempotent
operation UpdatePatientInfo {
    input: UpdatePatientInfoRequest
    output: PatientProfile
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/patients/medical-history", code: 200)
@readonly
operation GetMedicalHistory {
    output: MedicalHistoryResponse
    errors: [
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
