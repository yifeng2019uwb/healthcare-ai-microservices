package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.exception.ValidationException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Appointment entity representing scheduled appointments
 * Maps to appointments table
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @NotNull
    @Future(message = "Appointment must be scheduled in the future")
    @Column(name = "scheduled_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime scheduledAt;

    @Column(name = "checkin_time", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime checkinTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status = AppointmentStatus.AVAILABLE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType;

    @Size(max = 1000, message = "Appointment notes must not exceed 1000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "custom_data", columnDefinition = "JSONB")
    private String customData;

    // Constructors
    public Appointment() {}

    public Appointment(Provider provider, OffsetDateTime scheduledAt, AppointmentType appointmentType) {
        this.provider = provider;
        this.scheduledAt = scheduledAt;
        this.appointmentType = appointmentType;
        this.status = AppointmentStatus.AVAILABLE;
    }

    public Appointment(Patient patient, Provider provider, OffsetDateTime scheduledAt, AppointmentType appointmentType) {
        this.patient = patient;
        this.provider = provider;
        this.scheduledAt = scheduledAt;
        this.appointmentType = appointmentType;
        this.status = AppointmentStatus.SCHEDULED;
    }

    // Getters and Setters
    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(OffsetDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public OffsetDateTime getCheckinTime() {
        return checkinTime;
    }

    public void setCheckinTime(OffsetDateTime checkinTime) {
        this.checkinTime = checkinTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
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
     * Validates that the appointment has valid time data.
     *
     * @return true if appointment has valid time data, false otherwise
     */
    public boolean hasValidTimeData() {
        return scheduledAt != null && scheduledAt.isAfter(OffsetDateTime.now().minusYears(1));
    }

    /**
     * Validates that the appointment is completed.
     *
     * @return true if appointment is completed, false otherwise
     */
    public boolean isCompleted() {
        return status == AppointmentStatus.COMPLETED;
    }

    /**
     * Validates that the appointment is cancelled.
     *
     * @return true if appointment is cancelled, false otherwise
     */
    public boolean isCancelled() {
        return status == AppointmentStatus.CANCELLED;
    }

    /**
     * Validates that the appointment has a patient assigned.
     *
     * @return true if patient is assigned, false otherwise
     */
    public boolean hasPatient() {
        return patient != null;
    }

    /**
     * Validates that the appointment has a provider assigned.
     *
     * @return true if provider is assigned, false otherwise
     */
    public boolean hasProvider() {
        return provider != null;
    }

    /**
     * Validates that the appointment is ready for medical record creation.
     *
     * @return true if appointment is ready for medical records, false otherwise
     */
    public boolean isReadyForMedicalRecords() {
        return isCompleted() && hasPatient() && hasProvider();
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

    /**
     * Validates that the appointment is in a valid state for database operations.
     *
     * @return true if appointment is in valid state, false otherwise
     */
    public boolean isInValidState() {
        return scheduledAt != null && status != null;
    }

    /**
     * Validates that the scheduled time is at least 1 day after creation time.
     *
     * @return true if scheduled time is valid, false otherwise
     */
    public boolean hasValidScheduledTime() {
        if (getCreatedAt() == null || scheduledAt == null) {
            return false;
        }
        return scheduledAt.isAfter(getCreatedAt().plusDays(1));
    }

    /**
     * Pre-persist validation to ensure appointment scheduling rules are met.
     * This method is called automatically by JPA before saving the entity.
     *
     * @throws ValidationException if appointment scheduling constraints are violated
     */
    @PrePersist
    private void validateScheduledTime() {
        if (scheduledAt != null && getCreatedAt() != null) {
            if (!hasValidScheduledTime()) {
                throw new ValidationException("Appointment must be scheduled at least 1 day in advance");
            }
        }
    }

}
