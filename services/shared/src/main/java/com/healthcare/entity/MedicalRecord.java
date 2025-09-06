package com.healthcare.entity;

import com.healthcare.constants.DatabaseConstants;
import com.healthcare.enums.MedicalRecordType;
import com.healthcare.exception.ValidationException;
import com.healthcare.utils.ValidationUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Medical record entity representing patient medical records
 * Maps to medical_records table
 */
@Entity
@Table(name = DatabaseConstants.TABLE_MEDICAL_RECORDS,
       indexes = {
           @Index(name = DatabaseConstants.INDEX_MEDICAL_RECORDS_APPOINTMENT_TYPE, columnList = DatabaseConstants.COL_APPOINTMENT_ID + "," + DatabaseConstants.COL_RECORD_TYPE),
           @Index(name = DatabaseConstants.INDEX_MEDICAL_RECORDS_PATIENT_VISIBLE, columnList = DatabaseConstants.COL_APPOINTMENT_ID + "," + DatabaseConstants.COL_IS_PATIENT_VISIBLE + "," + DatabaseConstants.COL_RELEASE_DATE)
       })
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
    @Column(name = DatabaseConstants.COL_CONTENT, nullable = false, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TEXT)
    private String content;

    @Column(name = DatabaseConstants.COL_IS_PATIENT_VISIBLE, nullable = false)
    private Boolean isPatientVisible = false;

    @Column(name = DatabaseConstants.COL_RELEASE_DATE, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_TIMESTAMPTZ)
    private OffsetDateTime releaseDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = DatabaseConstants.COL_CUSTOM_DATA, columnDefinition = DatabaseConstants.COLUMN_DEFINITION_JSONB)
    private JsonNode customData;

    // ==================== JPA RELATIONSHIPS ====================

    /**
     * Many-to-one relationship with Appointment
     * Each medical record belongs to exactly one appointment
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = DatabaseConstants.COL_APPOINTMENT_ID, nullable = false, insertable = false, updatable = false)
    private Appointment appointment;

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
        this.content = ValidationUtils.validateRequiredStringWithLength(
            content,
            "Content",
            10,
            10000
        );
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

    public JsonNode getCustomData() {
        return customData;
    }

    public void setCustomData(JsonNode customData) {
        this.customData = customData;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    // ==================== VALIDATION METHODS ====================

    /**
     * Validates that the medical record is visible to patients.
     *
     * @return true if record is patient visible, false otherwise
     */
    public boolean isVisibleToPatient() {
        return isPatientVisible;
    }
}
