$version: "2.0"

namespace com.healthcare

use smithy.api#service
use smithy.api#http
use smithy.api#cors
use smithy.api#documentation

/// Healthcare AI Microservices - Main Service Definitions
/// This file defines all the services in the healthcare platform

/// Patient Service
/// Manages patient profiles, medical history, and patient-related operations
@service(sdkId: "patient-service")
@cors
@documentation("Patient Service - Manages patient profiles and medical history")
service PatientService {
    version: "1.0.0"
    operations: [
        CreatePatient,
        GetPatientProfile,
        UpdateProfile,
        UpdatePatientInfo,
        GetMedicalHistory,
        HealthCheck
    ]
}

/// Provider Service
/// Manages healthcare provider profiles, medical records, and provider operations
@service(sdkId: "provider-service")
@cors
@documentation("Provider Service - Manages healthcare provider profiles and medical records")
service ProviderService {
    version: "1.0.0"
    operations: [
        CreateProvider,
        GetProviderProfile,
        UpdateProfile,
        UpdateProfessionalInfo,
        SearchProviders,
        GetProviderDetails,
        CreateMedicalRecord,
        UpdateMedicalRecord,
        GetMedicalRecord,
        GetPatientMedicalRecords,
        HealthCheck
    ]
}

/// Appointment Service
/// Manages appointment scheduling, availability, and appointment lifecycle
@service(sdkId: "appointment-service")
@cors
@documentation("Appointment Service - Manages appointment scheduling and availability")
service AppointmentService {
    version: "1.0.0"
    operations: [
        BookAppointment,
        SearchAvailableSlots,
        GetPatientAppointments,
        CheckinAppointment,
        UpdateAppointmentStatus,
        GetProviderAppointments,
        CancelAppointment,
        SetProviderAvailability,
        HealthCheck
    ]
}

/// Auth Service
/// Handles JWT token validation and user context extraction
@service(sdkId: "auth-service")
@cors
@documentation("Auth Service - JWT token validation and user context extraction")
service AuthService {
    version: "1.0.0"
    operations: [
        ValidateToken,
        HealthCheck
    ]
}

/// Gateway Service
/// API Gateway that routes requests to appropriate services
@service(sdkId: "gateway-service")
@cors
@documentation("Gateway Service - API Gateway for routing and middleware")
service GatewayService {
    version: "1.0.0"
    operations: [
        HealthCheck
    ]
}
