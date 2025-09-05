package com.healthcare.entity;

import com.healthcare.enums.MedicalRecordType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
    void testMedicalRecordValidationMethods() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test medical record content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Test validation methods
        assertThat(record.hasAppointment()).isTrue();
        assertThat(record.hasValidRecordType()).isTrue();
        assertThat(record.hasValidContent()).isTrue();
        assertThat(record.isComplete()).isTrue();

        // Test patient visibility
        assertThat(record.isVisibleToPatient()).isFalse();
        record.setIsPatientVisible(true);
        assertThat(record.isVisibleToPatient()).isTrue();

        // Test update capability (before release)
        assertThat(record.canBeUpdated()).isTrue();

        // Test release functionality
        assertThat(record.hasBeenReleased()).isFalse();
        assertThat(record.canBeReleased()).isTrue();

        record.setReleaseDate(OffsetDateTime.now().minusDays(1));
        assertThat(record.hasBeenReleased()).isTrue();
        assertThat(record.canBeReleased()).isFalse();

        // Test update capability (after release)
        assertThat(record.canBeUpdated()).isFalse(); // Can't update after release
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
            assertThat(record.hasValidRecordType()).isTrue();
        }
    }

    @Test
    void testMedicalRecordEdgeCases() {
        // Test data variables
        UUID testAppointmentId = UUID.randomUUID();
        MedicalRecordType testRecordType = MedicalRecordType.DIAGNOSIS;
        String testContent = "Test content";

        MedicalRecord record = new MedicalRecord(testAppointmentId, testRecordType, testContent);

        // Test with null content
        record.setContent(null);
        assertThat(record.hasValidContent()).isFalse();
        assertThat(record.isComplete()).isFalse();

        // Test with empty content
        record.setContent("");
        assertThat(record.hasValidContent()).isFalse();

        // Test with valid content
        record.setContent("Valid content");
        assertThat(record.hasValidContent()).isTrue();
        assertThat(record.isComplete()).isTrue();

        // Test release date edge cases
        assertThat(record.hasBeenReleased()).isFalse();

        // Set future release date
        record.setReleaseDate(OffsetDateTime.now().plusDays(1));
        assertThat(record.hasBeenReleased()).isFalse();
        // canBeReleased() requires isPatientVisible to be true
        assertThat(record.canBeReleased()).isFalse(); // Not visible to patient yet

        // Set past release date
        record.setReleaseDate(OffsetDateTime.now().minusDays(1));
        assertThat(record.hasBeenReleased()).isTrue();
        assertThat(record.canBeReleased()).isFalse();
    }
}