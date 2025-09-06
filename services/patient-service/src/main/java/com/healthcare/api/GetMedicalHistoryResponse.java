package com.healthcare.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.constants.PatientServiceConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * API Response for getting medical history
 *
 * This matches the design document specification for GET /api/patients/medical-history
 *
 * @author Healthcare AI Team
 * @version 1.0.0
 * @since 2025-01-09
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GetMedicalHistoryResponse {

    @JsonProperty(PatientServiceConstants.JSON_FIELD_APPOINTMENTS)
    private List<Appointment> appointments;

    @JsonProperty(PatientServiceConstants.JSON_FIELD_SUMMARY)
    private Summary summary;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Appointment {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_ID)
        private String id;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_PROVIDER_ID)
        private String providerId;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_PROVIDER_NAME)
        private String providerName;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_SCHEDULED_AT)
        private String scheduledAt;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_STATUS)
        private String status;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_APPOINTMENT_TYPE)
        private String appointmentType;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_MEDICAL_RECORDS)
        private List<MedicalRecord> medicalRecords;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalRecord {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_ID)
        private String id;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_RECORD_TYPE)
        private String recordType;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_CONTENT)
        private String content;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_IS_PATIENT_VISIBLE)
        private boolean isPatientVisible;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_RELEASE_DATE)
        private String releaseDate;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_CREATED_AT)
        private String createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        @JsonProperty(PatientServiceConstants.JSON_FIELD_TOTAL_APPOINTMENTS)
        private int totalAppointments;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_COMPLETED_APPOINTMENTS)
        private int completedAppointments;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_UPCOMING_APPOINTMENTS)
        private int upcomingAppointments;

        @JsonProperty(PatientServiceConstants.JSON_FIELD_LAST_VISIT)
        private String lastVisit;
    }

    // Lombok generates: @NoArgsConstructor, @AllArgsConstructor, and all getters
}
