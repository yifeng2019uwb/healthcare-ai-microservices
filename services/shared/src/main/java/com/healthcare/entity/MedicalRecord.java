package com.healthcare.entity;

import com.healthcare.enums.MedicalRecordType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Medical record entity representing patient medical records
 * Maps to medical_records table
 */
@Entity
@Table(name = "medical_records")
public class MedicalRecord extends BaseEntity {

    /**
     * Foreign key linking to appointments.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false)
    private MedicalRecordType recordType;

    @NotBlank
    @Size(min = 10, max = 10000, message = "Medical record content must be between 10 and 10000 characters")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_patient_visible", nullable = false)
    private Boolean isPatientVisible = false;

    @Column(name = "release_date", columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime releaseDate;

    @Column(name = "custom_data", columnDefinition = "JSONB")
    private String customData;

    // Constructors
    public MedicalRecord() {}

    // Constructor for service layer (with appointment ID only)
    public MedicalRecord(UUID appointmentId, MedicalRecordType recordType, String content) {
        this.appointmentId = appointmentId;
        this.recordType = recordType;
        this.content = content;
        this.isPatientVisible = false; // Default value
    }

    // Getters and Setters

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public MedicalRecordType getRecordType() {
        return recordType;
    }

    public void setRecordType(MedicalRecordType recordType) {
        this.recordType = recordType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsPatientVisible() {
        return isPatientVisible;
    }

    public void setIsPatientVisible(Boolean isPatientVisible) {
        this.isPatientVisible = isPatientVisible;
    }

    public OffsetDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(OffsetDateTime releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the medical record has valid content.
     *
     * @return true if content is valid, false otherwise
     */
    public boolean hasValidContent() {
        return content != null && content.trim().length() >= 10 && content.trim().length() <= 10000;
    }

    /**
     * Validates that the medical record is visible to patients.
     *
     * @return true if record is patient visible, false otherwise
     */
    public boolean isVisibleToPatient() {
        return isPatientVisible != null && isPatientVisible;
    }

    /**
     * Validates that the medical record has been released to the patient.
     *
     * @return true if record has been released, false otherwise
     */
    public boolean hasBeenReleased() {
        return releaseDate != null && releaseDate.isBefore(OffsetDateTime.now());
    }

    /**
     * Validates that the medical record can be released to the patient.
     *
     * @return true if record can be released, false otherwise
     */
    public boolean canBeReleased() {
        return isVisibleToPatient() && !hasBeenReleased();
    }

    /**
     * Validates that the medical record has an associated appointment.
     *
     * @return true if appointment is present, false otherwise
     */
    public boolean hasAppointment() {
        return appointment != null;
    }

    /**
     * Validates that the medical record has a valid record type.
     *
     * @return true if record type is valid, false otherwise
     */
    public boolean hasValidRecordType() {
        return recordType != null;
    }

    /**
     * Validates that the medical record is complete and ready for storage.
     *
     * @return true if record is complete, false otherwise
     */
    public boolean isComplete() {
        return hasValidContent() && hasAppointment() && hasValidRecordType();
    }

    /**
     * Validates that the medical record can be updated.
     *
     * @return true if record can be updated, false otherwise
     */
    public boolean canBeUpdated() {
        return isComplete() && !hasBeenReleased();
    }
}
