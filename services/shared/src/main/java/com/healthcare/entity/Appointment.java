package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Appointment entity representing scheduled appointments
 * Maps to appointments table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_APPOINTMENTS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_APPOINTMENTS_PROVIDER_SCHEDULE, columnList = DatabaseConstants.COL_PROVIDER_ID + "," + DatabaseConstants.COL_STATUS + "," + DatabaseConstants.COL_SCHEDULED_AT),
           @Index(name = DatabaseConstants.INDEX_APPOINTMENTS_PATIENT_SCHEDULE, columnList = DatabaseConstants.COL_PATIENT_ID + "," + DatabaseConstants.COL_SCHEDULED_AT)
       })
public class Appointment extends BaseEntity {

    /**
     * Foreign key linking to patient_profiles.id
     * Nullable - appointment can be created without patient (available slot)
     * Immutable after creation - cannot be changed
     */
    @Column(name = DatabaseConstants.COL_PATIENT_ID)
    private UUID patientId;

    /**
     * Foreign key linking to provider_profiles.id
     * Required - every appointment must have a provider
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = DatabaseConstants.COL_PROVIDER_ID, nullable = false)
    private UUID providerId;

    @NotNull
    @Future(message = "Appointment must be scheduled in the future")
    @Column(name = DatabaseConstants.COL_SCHEDULED_AT, nullable = false, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime scheduledAt;

    @Column(name = DatabaseConstants.COL_CHECKIN_TIME, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime checkinTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_APPOINTMENT_STATUS, nullable = false)
    private AppointmentStatus status = AppointmentStatus.AVAILABLE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_APPOINTMENT_TYPE, nullable = false)
    private AppointmentType appointmentType;

    @Size(max = 1000, message = "Appointment notes must not exceed 1000 characters")
    @Column(name = DatabaseConstants.COL_NOTES, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode customData;

    // ==================== JPA RELATIONSHIPS ====================

    /**
     * Many-to-one relationship with Patient
     * An appointment can belong to one patient (nullable for available slots)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_PATIENT_ID, insertable = false, updatable = false)
    private Patient patient;

    /**
     * Many-to-one relationship with Provider
     * An appointment must belong to one provider
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_PROVIDER_ID, nullable = false, insertable = false, updatable = false)
    private Provider provider;

    /**
     * One-to-many relationship with MedicalRecords
     * An appointment can have multiple medical records
     */
    @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private java.util.List<MedicalRecord> medicalRecords = new java.util.ArrayList<>();

    // Constructors
    public Appointment() {}

    // Constructor for provider slot creation (no patient yet)
    public Appointment(UUID providerId, OffsetDateTime scheduledAt, AppointmentType appointmentType) {
        this.providerId = providerId;
        this.scheduledAt = scheduledAt;
        this.appointmentType = appointmentType;
        this.status = AppointmentStatus.AVAILABLE;
    }

    // Getters and Setters

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        if (providerId == null) {
            throw new ValidationException("Provider ID cannot be null");
        }
        this.providerId = providerId;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(OffsetDateTime scheduledAt) {
        if (scheduledAt == null) {
            throw new ValidationException("Scheduled time cannot be null");
        }

        // Validate that appointment is scheduled at least 1 day in advance
        OffsetDateTime oneDayFromNow = OffsetDateTime.now().plusDays(1);
        if (scheduledAt.isBefore(oneDayFromNow)) {
            throw new ValidationException("Appointment must be scheduled at least 1 day in advance");
        }

        this.scheduledAt = scheduledAt;
    }

    public OffsetDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(OffsetDateTime checkinTime) {
        // Checkin time can be null (patient hasn't checked in yet)
        if (checkinTime == null) {
            this.checkinTime = null;
            return;
        }

        // If scheduledAt is set, validate checkin time is within reasonable range
        if (scheduledAt != null) {
            // Checkin should be within 2 hours before or after scheduled time
            OffsetDateTime twoHoursBefore = scheduledAt.minusHours(2);
            OffsetDateTime twoHoursAfter = scheduledAt.plusHours(2);

            if (checkinTime.isBefore(twoHoursBefore) || checkinTime.isAfter(twoHoursAfter)) {
                throw new ValidationException("Check-in time must be within 2 hours of scheduled appointment time");
            }
        }

        this.checkinTime = checkinTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        if (status == null) {
            throw new ValidationException("Appointment status cannot be null");
        }
        this.status = status;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        if (appointmentType == null) {
            throw new ValidationException("Appointment type cannot be null");
        }
        this.appointmentType = appointmentType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = ValidationUtils.validateAndNormalizeString(
            notes,
            "Appointment notes",
            1000
        );
    }

    public JsonNode getCustomData() {
        return customData;
    }

    public void setCustomData(JsonNode customData) {
        this.customData = customData;
    }

    public Patient getPatient() {
        return patient;
    }

    public Provider getProvider() {
        return provider;
    }

    public java.util.List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the appointment is scheduled in the future.
     *
     * @return true if appointment is in the future, false otherwise
     */
    public boolean isScheduledInFuture() {
        if (scheduledAt == null) {
            return false;
        }
        return scheduledAt.isAfter(OffsetDateTime.now());
    }


    /**
     * Validates that the appointment has a patient assigned.
     *
     * @return true if patient is assigned, false otherwise
     */
    public boolean hasPatient() {
        return patientId != null;
    }


    /**
     * Validates that the appointment is ready for medical record creation.
     * Allows both IN_PROGRESS and COMPLETED statuses.
     *
     * @return true if appointment is ready for medical records, false otherwise
     */
    public boolean isReadyForMedicalRecords() {
        return (status == AppointmentStatus.IN_PROGRESS || status == AppointmentStatus.COMPLETED) && hasPatient();
    }

    /**
     * Validates that the appointment has expired due to time passing.
     * An appointment expires if it's scheduled in the past and not completed.
     *
     * @return true if appointment has expired, false otherwise
     */
    public boolean hasExpired() {
        if (scheduledAt == null) {
            return false;
        }
        return scheduledAt.isBefore(OffsetDateTime.now()) &&
               (status == AppointmentStatus.AVAILABLE || status == AppointmentStatus.SCHEDULED);
    }

    /**
     * Validates that the appointment is no longer valid for booking.
     * An appointment becomes invalid if it's expired or already booked.
     *
     * @return true if appointment is invalid for booking, false otherwise
     */
    public boolean isInvalidForBooking() {
        return hasExpired() || status == AppointmentStatus.SCHEDULED ||
               status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED;
    }


}
