package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.MedicalRecordType;
import com.healthcare.exception.ValidationException;
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
@Table(name = DatabaseConstants.TABLE_MEDICAL_RECORDS)
public class MedicalRecord extends BaseEntity {

    /**
     * Foreign key linking to appointments.id
     * Immutable after creation - cannot be changed
     */
    @NotNull
    @Column(name = DatabaseConstants.COL_APPOINTMENT_ID, nullable = false)
    private UUID appointmentId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = DatabaseConstants.COL_RECORD_TYPE, nullable = false)
    private MedicalRecordType recordType;

    @NotBlank
    @Size(min = 10, max = 10000, message = "Medical record content must be between 10 and 10000 characters")
    @Column(name = DatabaseConstants.COL_CONTENT, nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = DatabaseConstants.COL_IS_PATIENT_VISIBLE, nullable = false)
    private Boolean isPatientVisible = false;

    @Column(name = DatabaseConstants.COL_RELEASE_DATE, columnDefinition = "TIMESTAMPTZ")
    private OffsetDateTime releaseDate;

    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = "JSONB")
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
        if (recordType == null) {
            throw new ValidationException("Record type cannot be null");
        }
        this.recordType = recordType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Content cannot be null or empty");
        }
        if (content.trim().length() < 10) {
            throw new ValidationException("Content must be at least 10 characters");
        }
        if (content.length() > 10000) {
            throw new ValidationException("Content cannot exceed 10000 characters");
        }
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
     * Validates that the medical record is visible to patients.
     *
     * @return true if record is patient visible, false otherwise
     */
    public boolean isVisibleToPatient() {
        return isPatientVisible != null && isPatientVisible;
    }
}
