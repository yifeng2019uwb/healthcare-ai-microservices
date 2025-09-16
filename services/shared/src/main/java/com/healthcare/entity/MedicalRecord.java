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
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_MEDICAL_RECORD_WITH_APPOINTMENT,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_APPOINTMENT)
        }
    ),
    @NamedEntityGraph(
        name = DatabaseConstants.ENTITY_GRAPH_MEDICAL_RECORD_FULL_DETAILS,
        attributeNodes = {
            @NamedAttributeNode(DatabaseConstants.ATTR_APPOINTMENT)
        }
    )
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
    @Size(min = 3, max = 10000, message = "Medical record content must be between 3 and 10000 characters")
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

    // ==================== CONSTRUCTORS ====================

    /**
     * Private constructor for JPA only.
     */
    @SuppressWarnings("unused")
    private MedicalRecord() {}

    /**
     * Simple constructor for required fields only.
     * Use setters for optional fields.
     *
     * @param appointmentId The ID of the appointment this record belongs to
     * @param recordType The type of medical record (DIAGNOSIS, TREATMENT, etc.)
     * @param content The medical record content (10-10000 characters)
     */
    public MedicalRecord(UUID appointmentId, MedicalRecordType recordType, String content) {
        if (appointmentId == null) {
            throw new ValidationException("Appointment ID is required");
        }
        if (recordType == null) {
            throw new ValidationException("Record type is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Content is required");
        }
        if (content.length() < 3) {
            throw new ValidationException("Content must be at least 3 characters");
        }
        if (content.length() > 10000) {
            throw new ValidationException("Content cannot exceed 10000 characters");
        }

        this.appointmentId = appointmentId;
        this.recordType = recordType;
        this.content = content;
        this.isPatientVisible = false; // Default: not visible to patient
    }


    // Getters and Setters

    public UUID getAppointmentId() {
        return appointmentId;
    }

    // Note: appointmentId is immutable after creation - use factory methods to create new records

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
            3,
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

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Validates that the medical record object is in a valid state.
     * This should be called after object creation to ensure all required fields are set.
     *
     * @throws ValidationException if the medical record is in an invalid state
     */
    public void validateState() {
        if (appointmentId == null) {
            throw new ValidationException("Appointment ID is required");
        }
        if (recordType == null) {
            throw new ValidationException("Record type is required");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException("Content is required");
        }
        if (content.length() < 10 || content.length() > 10000) {
            throw new ValidationException("Content must be between 10 and 10000 characters");
        }
    }

    /**
     * Checks if this medical record is visible to patients.
     *
     * @return true if record is patient visible, false otherwise
     */
    public boolean isVisibleToPatient() {
        return isPatientVisible;
    }

    /**
     * Checks if this medical record has been released to the patient.
     * A record is considered released if it's patient visible and the release date has passed.
     *
     * @return true if record is released to patient and release date has passed, false otherwise
     */
    public boolean isReleasedToPatient() {
        return isPatientVisible && releaseDate != null && releaseDate.isBefore(OffsetDateTime.now());
    }

    /**
     * Immediately releases this medical record to the patient.
     * Sets visibility to true and release date to current time.
     */
    public void releaseToPatient() {
        this.isPatientVisible = true;
        this.releaseDate = OffsetDateTime.now();
    }

    /**
     * Schedules this medical record for future release to the patient.
     *
     * @param releaseDate The date and time when the record should be released
     * @throws ValidationException if release date is in the past
     */
    public void scheduleRelease(OffsetDateTime releaseDate) {
        if (releaseDate == null) {
            throw new ValidationException("Release date cannot be null");
        }
        if (releaseDate.isBefore(OffsetDateTime.now())) {
            throw new ValidationException("Release date cannot be in the past");
        }
        this.isPatientVisible = true;
        this.releaseDate = releaseDate;
    }
}
