$version: "2.0"

namespace com.healthcare

use com.healthcare#Address
use com.healthcare#AppointmentStatus
use com.healthcare#AppointmentType
use com.healthcare#BadRequestError
use com.healthcare#Email
use com.healthcare#HealthCheckResponse
use com.healthcare#InternalServerError
use com.healthcare#Name
use com.healthcare#NotFoundError
use com.healthcare#PaginationResponse
use com.healthcare#Phone
use com.healthcare#SuccessResponse
use com.healthcare#UnauthorizedError
use com.healthcare#Uuid

/// Appointment Service API Models
/// Book appointment request
structure BookAppointmentRequest {
    @httpLabel
    @required
    id: Uuid

    @required
    patientId: Uuid

    @required
    @length(min: 1, max: 500)
    reason: String

    @length(min: 1, max: 1000)
    notes: String
}

/// Search available slots request
structure SearchAvailableSlotsRequest {
    @httpQuery("providerId")
    providerId: Uuid

    @httpQuery("specialty")
    @length(min: 1, max: 100)
    specialty: String

    @httpQuery("date")
    @timestampFormat("date-time")
    date: Timestamp

    @httpQuery("startDate")
    @timestampFormat("date-time")
    startDate: Timestamp

    @httpQuery("endDate")
    @timestampFormat("date-time")
    endDate: Timestamp

    @httpQuery("appointmentType")
    appointmentType: AppointmentType

    @httpQuery("page")
    @range(min: 1)
    page: Integer = 1

    @httpQuery("size")
    @range(min: 1, max: 50)
    size: Integer = 20
}

/// Patient check-in request
structure CheckinRequest {
    @httpLabel
    @required
    id: Uuid

    @length(min: 1, max: 1000)
    notes: String
}

/// Update appointment status request
structure UpdateStatusRequest {
    @httpLabel
    @required
    id: Uuid

    @required
    status: AppointmentStatus

    @length(min: 1, max: 1000)
    notes: String
}

/// Set provider availability request
structure SetAvailabilityRequest {
    @required
    @timestampFormat("date-time")
    date: Timestamp

    @required
    @timestampFormat("date-time")
    startTime: Timestamp

    @required
    @timestampFormat("date-time")
    endTime: Timestamp

    @required
    appointmentType: AppointmentType

    @required
    @range(min: 15, max: 480)
    slotDurationMinutes: Integer

    @length(min: 1, max: 1000)
    notes: String
}

/// Cancel appointment request
structure CancelAppointmentRequest {
    @httpLabel
    @required
    id: Uuid

    @required
    @length(min: 1, max: 1000)
    reason: String
}

/// Time slot structure
structure TimeSlot {
    @required
    id: Uuid

    @required
    @timestampFormat("date-time")
    startTime: Timestamp

    @required
    @timestampFormat("date-time")
    endTime: Timestamp

    @required
    appointmentType: AppointmentType

    @required
    status: AppointmentStatus

    @required
    @range(min: 15, max: 480)
    durationMinutes: Integer
}

/// Provider information for appointments
structure ProviderInfo {
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

    address: Address

    phone: Phone

    email: Email
}

/// Patient information for appointments
structure PatientInfo {
    @required
    id: Uuid

    @required
    firstName: Name

    @required
    lastName: Name

    phone: Phone

    email: Email

    address: Address
}

/// Appointment response
structure AppointmentResponse {
    @required
    id: Uuid

    @required
    provider: ProviderInfo

    @required
    patient: PatientInfo

    @required
    @timestampFormat("date-time")
    scheduledTime: Timestamp

    @required
    @timestampFormat("date-time")
    endTime: Timestamp

    @required
    appointmentType: AppointmentType

    @required
    status: AppointmentStatus

    @length(min: 1, max: 500)
    reason: String

    @length(min: 1, max: 1000)
    notes: String

    @timestampFormat("date-time")
    checkinTime: Timestamp

    @timestampFormat("date-time")
    completedTime: Timestamp

    @length(min: 1, max: 1000)
    providerNotes: String
}

/// Available slots response
structure AvailableSlotsResponse {
    @required
    slots: TimeSlotList

    @required
    pagination: PaginationResponse
}

/// Appointment list response
structure AppointmentListResponse {
    @required
    appointments: AppointmentList

    @required
    pagination: PaginationResponse
}

/// Appointment service operations
@http(method: "PUT", uri: "/api/appointments/{id}", code: 200)
@idempotent
operation BookAppointment {
    input: BookAppointmentRequest
    output: AppointmentResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/appointments/available-slots", code: 200)
@readonly
operation SearchAvailableSlots {
    input: SearchAvailableSlotsRequest
    output: AvailableSlotsResponse
    errors: [
        BadRequestError
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/appointments", code: 200)
@readonly
operation GetPatientAppointments {
    input: GetPatientAppointmentsRequest
    output: AppointmentListResponse
    errors: [
        BadRequestError
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/appointments/{id}/checkin", code: 200)
@idempotent
operation CheckinAppointment {
    input: CheckinRequest
    output: AppointmentResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/appointments/{id}/status", code: 200)
@idempotent
operation UpdateAppointmentStatus {
    input: UpdateStatusRequest
    output: AppointmentResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/api/appointments/provider/{providerId}", code: 200)
@readonly
operation GetProviderAppointments {
    input: GetProviderAppointmentsRequest
    output: AppointmentListResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "PUT", uri: "/api/appointments/{id}/cancel", code: 200)
@idempotent
operation CancelAppointment {
    input: CancelAppointmentRequest
    output: AppointmentResponse
    errors: [
        BadRequestError
        UnauthorizedError
        NotFoundError
        InternalServerError
    ]
}

@http(method: "POST", uri: "/api/appointments/availability", code: 201)
operation SetProviderAvailability {
    input: SetAvailabilityRequest
    output: SuccessResponse
    errors: [
        BadRequestError
        UnauthorizedError
        InternalServerError
    ]
}

@http(method: "GET", uri: "/health", code: 200)
@readonly
operation HealthCheck {
    output: HealthCheckResponse
}

/// Request structures for path parameters and query parameters
structure GetPatientAppointmentsRequest {
    @httpQuery("status")
    status: AppointmentStatus

    @httpQuery("startDate")
    @timestampFormat("date-time")
    startDate: Timestamp

    @httpQuery("endDate")
    @timestampFormat("date-time")
    endDate: Timestamp

    @httpQuery("page")
    @range(min: 1)
    page: Integer = 1

    @httpQuery("size")
    @range(min: 1, max: 50)
    size: Integer = 20
}

structure GetProviderAppointmentsRequest {
    @httpLabel
    @required
    providerId: Uuid

    @httpQuery("status")
    status: AppointmentStatus

    @httpQuery("startDate")
    @timestampFormat("date-time")
    startDate: Timestamp

    @httpQuery("endDate")
    @timestampFormat("date-time")
    endDate: Timestamp

    @httpQuery("page")
    @range(min: 1)
    page: Integer = 1

    @httpQuery("size")
    @range(min: 1, max: 50)
    size: Integer = 20
}
