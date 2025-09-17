$version: "2.0"

namespace com.healthcare

use com.healthcare#UserRole
use com.healthcare#UserStatus
use com.healthcare#Gender
use com.healthcare#Address
use com.healthcare#ContactInfo
use com.healthcare#AuditInfo
use com.healthcare#SuccessResponse
use com.healthcare#HealthCheckResponse
use com.healthcare#PaginationRequest
use com.healthcare#PaginationResponse
use com.healthcare#BadRequestError
use com.healthcare#UnauthorizedError
use com.healthcare#NotFoundError
use com.healthcare#InternalServerError
use com.healthcare#Name
use com.healthcare#Phone
use com.healthcare#Email
use com.healthcare#Uuid
use com.healthcare#PatientNumber

/// Patient Service API Models

/// Create patient account request
structure CreatePatientRequest {
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

/// Update personal profile request
structure UpdateProfileRequest {
    firstName: Name
    lastName: Name
    phone: Phone
    email: Email
    address: Address
    emergencyContactPhone: Phone
}

/// Medical history structure
structure MedicalHistory {
    conditions: [String]
    surgeries: [String]
    medications: [String]
    allergies: [String]
    familyHistory: [String]
}

/// Allergies structure
structure Allergies {
    knownAllergies: [String]
    reactions: [String]
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

/// User profile response
structure UserProfile {
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
    appointments: [AppointmentSummary]
    medicalRecords: [MedicalRecordSummary]
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
    input: CreatePatientRequest
    output: SuccessResponse
    errors: [BadRequestError, UnauthorizedError, InternalServerError]
}

@http(method: "GET", uri: "/api/patients/profile", code: 200)
operation GetPatientProfile {
    output: UserProfile
    errors: [UnauthorizedError, NotFoundError, InternalServerError]
}

@http(method: "PUT", uri: "/api/patients/profile", code: 200)
operation UpdateProfile {
    input: UpdateProfileRequest
    output: UserProfile
    errors: [BadRequestError, UnauthorizedError, NotFoundError, InternalServerError]
}

@http(method: "PUT", uri: "/api/patients/patient-info", code: 200)
operation UpdatePatientInfo {
    input: UpdatePatientInfoRequest
    output: PatientProfile
    errors: [BadRequestError, UnauthorizedError, NotFoundError, InternalServerError]
}

@http(method: "GET", uri: "/api/patients/medical-history", code: 200)
operation GetMedicalHistory {
    output: MedicalHistoryResponse
    errors: [UnauthorizedError, NotFoundError, InternalServerError]
}

@http(method: "GET", uri: "/health", code: 200)
operation HealthCheck {
    output: HealthCheckResponse
}
