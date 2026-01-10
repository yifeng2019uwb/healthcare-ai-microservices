$version: "2.0"

namespace com.healthcare

/// Patient Service
service PatientService {
    version: "1.0.0"
    operations: [
        CreatePatient
        GetPatientProfile
        UpdatePatientProfile
        UpdatePatientInfo
        GetMedicalHistory
        HealthCheck
    ]
}

/// Provider Service
service ProviderService {
    version: "1.0.0"
    operations: [
        CreateProvider
        GetProviderProfile
        UpdateProviderProfile
        SearchProviders
        GetProviderDetails
        CreateMedicalRecord
        UpdateMedicalRecord
        GetMedicalRecord
        GetPatientMedicalRecords
        HealthCheck
    ]
}

/// Appointment Service
service AppointmentService {
    version: "1.0.0"
    operations: [
        BookAppointment
        SearchAvailableSlots
        GetPatientAppointments
        CheckinAppointment
        UpdateAppointmentStatus
        GetProviderAppointments
        CancelAppointment
        SetProviderAvailability
        HealthCheck
    ]
}

/// Auth Service
service AuthService {
    version: "1.0.0"
    operations: [
        ValidateToken
        HealthCheck
    ]
}

/// Gateway Service
service GatewayService {
    version: "1.0.0"
    operations: [
        RegisterPatient
        RegisterProvider
        HealthCheck
    ]
}
