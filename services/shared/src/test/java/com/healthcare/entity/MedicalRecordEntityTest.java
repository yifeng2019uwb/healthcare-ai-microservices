package com.healthcare.entity;

import com.healthcare.enums.MedicalRecordType;
import com.healthcare.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for MedicalRecord entity
 */
class MedicalRecordEntityTest {

    @Test
    void testMedicalRecordEntity() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testInitialContent = "Initial Consultation - Hypertension diagnosis";
        String testUpdatedContent = "Updated diagnosis: Hypertension stage 1";
        JsonNode testCustomData = null; // Will be set directly as JsonNode
        boolean testIsPatientVisible = true;

        // Create medical record
        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testInitialContent);
        record.setIsPatientVisible(testIsPatientVisible);

        // Test basic properties
        assertThat(record.getAppointmentId()).isEqualTo(testAppointmentId);
        assertThat(record.getRecordType()).isEqualTo(testRecordType);
        assertThat(record.getContent()).isEqualTo(testInitialContent);
        assertThat(record.getIsPatientVisible()).isTrue();

        // Test additional setters
        record.setContent(testUpdatedContent);
        record.setReleaseDate(OffsetDateTime.now().plusDays(1));
        record.setCustomData(testCustomData);

        assertThat(record.getContent()).isEqualTo(testUpdatedContent);
        assertThat(record.getReleaseDate()).isNotNull();
        assertThat(record.getCustomData()).isNull();
    }

    @Test
    void testAppointmentIdField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);
        assertThat(record.getAppointmentId()).isEqualTo(testAppointmentId);
    }

    @Test
    void testRecordTypeField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "This is a valid medical record content that meets the minimum length requirement";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);
        assertThat(record.getRecordType()).isEqualTo(testRecordType);

        // Test setter with valid record type
        MedicalRecordType newRecordType = MedicalRecordType.TREATMENT;
        record.setRecordType(newRecordType);
        assertThat(record.getRecordType()).isEqualTo(newRecordType);

        // Test setter with null record type - should throw ValidationException
        assertThatThrownBy(() -> record.setRecordType(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Record type cannot be null");
    }


    @Test
    void testIsPatientVisibleField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);
        assertThat(record.isVisibleToPatient()).isFalse();

        record.setIsPatientVisible(true);
        assertThat(record.isVisibleToPatient()).isTrue();
    }

    @Test
    void testReleaseDateField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);
        assertThat(record.getReleaseDate()).isNull();

        OffsetDateTime testReleaseDate = OffsetDateTime.now().minusDays(1);
        record.setReleaseDate(testReleaseDate);
        assertThat(record.getReleaseDate()).isEqualTo(testReleaseDate);
    }

    @Test
    void testMedicalRecordTypes() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        String testContent = "Test content for different record types";

        // Test different record types
        for (MedicalRecordType recordType : MedicalRecordType.values()) {
            MedicalRecord record = new MedicalRecord(testAppointmentId, recordType, testContent);
            assertThat(record.getRecordType()).isEqualTo(recordType);
            assertThat(record.getRecordType()).isNotNull();
        }
    }

    @Test
    void testContentField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "This is a valid medical record content that meets the minimum length requirement";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Test with valid content
        record.setContent(testContent);
        assertThat(record.getContent()).isEqualTo(testContent);

        // Test with trimmed content
        record.setContent("  " + testContent + "  ");
        assertThat(record.getContent()).isEqualTo(testContent);

        // Test with null content - should throw validation exception
        assertThatThrownBy(() -> record.setContent(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content cannot be null or empty");

        // Test with empty content - should throw validation exception
        assertThatThrownBy(() -> record.setContent(""))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content cannot be null or empty");

        // Test with whitespace only content - should throw validation exception
        assertThatThrownBy(() -> record.setContent("   "))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content cannot be null or empty");

        // Test with content too short (less than 10 characters) - should throw validation exception
        assertThatThrownBy(() -> record.setContent("Short"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content must be at least 10 characters");

        // Test with content exactly 10 characters - should be valid
        String exactlyTenChars = "1234567890";
        record.setContent(exactlyTenChars);
        assertThat(record.getContent()).isEqualTo(exactlyTenChars);

        // Test with content exactly 10000 characters - should be valid
        String exactlyMaxChars = "a".repeat(10000);
        record.setContent(exactlyMaxChars);
        assertThat(record.getContent()).isEqualTo(exactlyMaxChars);

        // Test with content exceeding 10000 characters - should throw validation exception
        String tooLongContent = "a".repeat(10001);
        assertThatThrownBy(() -> record.setContent(tooLongContent))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content cannot exceed 10000 characters");
    }

    @Test
    void testCustomDataField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "This is a valid medical record content that meets the minimum length requirement";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Test initial state
        assertThat(record.getCustomData()).isNull();

        // Test setter with null value (allowed)
        record.setCustomData(null);
        assertThat(record.getCustomData()).isNull();

        // Note: JsonNode creation and validation should be handled at service layer
        // Entity only accepts pre-validated JsonNode objects
    }

    @Test
    void testReleaseDateSetter() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Test with null release date
        assertThat(record.getReleaseDate()).isNull();

        // Set future release date
        OffsetDateTime futureDate = OffsetDateTime.now().plusDays(1);
        record.setReleaseDate(futureDate);
        assertThat(record.getReleaseDate()).isEqualTo(futureDate);

        // Set past release date
        OffsetDateTime pastDate = OffsetDateTime.now().minusDays(1);
        record.setReleaseDate(pastDate);
        assertThat(record.getReleaseDate()).isEqualTo(pastDate);
    }

    @Test
    void testGetAppointment() {
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content for appointment test";
        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Initially should be null
        assertThat(record.getAppointment()).isNull();

        // Create an appointment and set it (using reflection since there's no setter)
        Appointment appointment = new Appointment();
        try {
            java.lang.reflect.Field appointmentField = MedicalRecord.class.getDeclaredField("appointment");
            appointmentField.setAccessible(true);
            appointmentField.set(record, appointment);
        } catch (Exception e) {
            fail("Failed to set appointment field via reflection: " + e.getMessage());
        }

        // Now should return the appointment
        assertThat(record.getAppointment()).isEqualTo(appointment);
    }
}