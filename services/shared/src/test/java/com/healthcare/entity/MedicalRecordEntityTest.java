package com.healthcare.entity;

import com.healthcare.enums.MedicalRecordType;
import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        String testCustomData = "{\"priority\": \"high\"}";
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
        assertThat(record.getCustomData()).isEqualTo(testCustomData);
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
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);
        assertThat(record.getRecordType()).isEqualTo(testRecordType);
    }

    @Test
    void testContentField() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);
        assertThat(record.getContent()).isEqualTo(testContent);
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
    void testContentSetter() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Test with null content - should throw validation exception
        assertThatThrownBy(() -> record.setContent(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content cannot be null or empty");

        // Test with empty content - should throw validation exception
        assertThatThrownBy(() -> record.setContent(""))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Content cannot be null or empty");

        // Test with valid content
        record.setContent("Valid content");
        assertThat(record.getContent()).isEqualTo("Valid content");
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
}