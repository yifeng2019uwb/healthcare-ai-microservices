package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Appointment entity
 * Organized by test strategy: constructors, fields, validation methods
 */
class AppointmentEntityTest {

    // Reusable test data
    private static final UUID testProviderId = UUID.randomUUID();
    private static final UUID testPatientId = UUID.randomUUID();
    private static final OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2); // More than 1 day in advance
    private static final AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void testAppointmentConstructors() {
        // Test default constructor
        Appointment appointment = new Appointment();
        assertThat(appointment.getProviderId()).isNull();
        assertThat(appointment.getScheduledAt()).isNull();
        assertThat(appointment.getAppointmentType()).isNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE); // Default value
        assertThat(appointment.getPatientId()).isNull();
        assertThat(appointment.getNotes()).isNull();
        assertThat(appointment.getCustomData()).isNull();
        assertThat(appointment.getCheckinTime()).isNull();

        // Test parameterized constructor
        appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThat(appointment.getProviderId()).isEqualTo(testProviderId);
        assertThat(appointment.getScheduledAt()).isEqualTo(testScheduledAt);
        assertThat(appointment.getAppointmentType()).isEqualTo(testAppointmentType);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE);
        assertThat(appointment.getPatientId()).isNull();
        assertThat(appointment.getNotes()).isNull();
        assertThat(appointment.getCustomData()).isNull();
        assertThat(appointment.getCheckinTime()).isNull();
    }

    // ==================== FIELD TESTS ====================

    @Test
    void testPatientIdField() {
        Appointment appointment = new Appointment();

        // Test initial state
        assertThat(appointment.getPatientId()).isNull();

        // Test setter
        appointment.setPatientId(testPatientId);
        assertThat(appointment.getPatientId()).isEqualTo(testPatientId);

        // Test setter with null
        appointment.setPatientId(null);
        assertThat(appointment.getPatientId()).isNull();
    }

    @Test
    void testProviderIdField() {
        Appointment appointment = new Appointment();
        UUID newProviderId = UUID.randomUUID();

        // Test initial state
        assertThat(appointment.getProviderId()).isNull();

        // Test setter
        appointment.setProviderId(newProviderId);
        assertThat(appointment.getProviderId()).isEqualTo(newProviderId);

        // Test setter with null (should throw ValidationException)
        assertThatThrownBy(() -> appointment.setProviderId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Provider ID cannot be null");
    }

    @Test
    void testScheduledAtField() {
        Appointment appointment = new Appointment();
        OffsetDateTime validScheduledAt = OffsetDateTime.now().plusDays(2);

        // Test initial state
        assertThat(appointment.getScheduledAt()).isNull();

        // Test setter with valid time (more than 1 day in advance)
        appointment.setScheduledAt(validScheduledAt);
        assertThat(appointment.getScheduledAt()).isEqualTo(validScheduledAt);

        // Test setter with null (should throw ValidationException)
        assertThatThrownBy(() -> appointment.setScheduledAt(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Scheduled time cannot be null");

        // Test setter with time less than 1 day in advance (should throw ValidationException)
        OffsetDateTime invalidScheduledAt = OffsetDateTime.now().plusHours(12);
        assertThatThrownBy(() -> appointment.setScheduledAt(invalidScheduledAt))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment must be scheduled at least 1 day in advance");

        // Test setter with past time (should throw ValidationException)
        OffsetDateTime pastTime = OffsetDateTime.now().minusDays(1);
        assertThatThrownBy(() -> appointment.setScheduledAt(pastTime))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment must be scheduled at least 1 day in advance");
    }

    @Test
    void testCheckinTimeField() {
        Appointment appointment = new Appointment();
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(2);
        OffsetDateTime validCheckinTime = scheduledAt.minusMinutes(30);

        // Test initial state
        assertThat(appointment.getCheckinTime()).isNull();

        // Test setter with null (should be allowed)
        appointment.setCheckinTime(null);
        assertThat(appointment.getCheckinTime()).isNull();

        // Test setter with valid checkin time (no scheduledAt set yet - should be allowed)
        OffsetDateTime checkinTime = OffsetDateTime.now();
        appointment.setCheckinTime(checkinTime);
        assertThat(appointment.getCheckinTime()).isEqualTo(checkinTime);

        // Set scheduledAt first
        appointment.setScheduledAt(scheduledAt);

        // Test setter with valid checkin time (within 2 hours of scheduled time)
        appointment.setCheckinTime(validCheckinTime);
        assertThat(appointment.getCheckinTime()).isEqualTo(validCheckinTime);

        // Test setter with checkin time too early (more than 2 hours before)
        OffsetDateTime tooEarly = scheduledAt.minusHours(3);
        assertThatThrownBy(() -> appointment.setCheckinTime(tooEarly))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Check-in time must be within 2 hours of scheduled appointment time");

        // Test setter with checkin time too late (more than 2 hours after)
        OffsetDateTime tooLate = scheduledAt.plusHours(3);
        assertThatThrownBy(() -> appointment.setCheckinTime(tooLate))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Check-in time must be within 2 hours of scheduled appointment time");
    }

    @Test
    void testStatusField() {
        Appointment appointment = new Appointment();

        // Test initial state (defaults to AVAILABLE)
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE);

        // Test setter with each status
        for (AppointmentStatus status : AppointmentStatus.values()) {
            appointment.setStatus(status);
            assertThat(appointment.getStatus()).isEqualTo(status);
        }

        // Test setter with null (should throw ValidationException)
        assertThatThrownBy(() -> appointment.setStatus(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment status cannot be null");
    }

    @Test
    void testAppointmentTypeField() {
        Appointment appointment = new Appointment();

        // Test initial state
        assertThat(appointment.getAppointmentType()).isNull();

        // Test setter with each type
        for (AppointmentType type : AppointmentType.values()) {
            appointment.setAppointmentType(type);
            assertThat(appointment.getAppointmentType()).isEqualTo(type);
        }

        // Test setter with null (should throw ValidationException)
        assertThatThrownBy(() -> appointment.setAppointmentType(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment type cannot be null");
    }

    @Test
    void testNotesField() {
        Appointment appointment = new Appointment();
        String testNotes = "Patient has allergies";

        // Test initial state
        assertThat(appointment.getNotes()).isNull();

        // Test setter with valid notes
        appointment.setNotes(testNotes);
        assertThat(appointment.getNotes()).isEqualTo(testNotes);

        // Test setter with null (should normalize to null)
        appointment.setNotes(null);
        assertThat(appointment.getNotes()).isNull();

        // Test setter with empty string (should normalize to null)
        appointment.setNotes("");
        assertThat(appointment.getNotes()).isNull();

        // Test setter with whitespace only (should normalize to null)
        appointment.setNotes("   ");
        assertThat(appointment.getNotes()).isNull();

        // Test setter with trimmed valid notes
        appointment.setNotes("  Patient has allergies  ");
        assertThat(appointment.getNotes()).isEqualTo("Patient has allergies");

        // Test setter with notes exceeding 1000 characters (should throw ValidationException)
        String longNotes = "a".repeat(1001);
        assertThatThrownBy(() -> appointment.setNotes(longNotes))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment notes cannot exceed 1000 characters");

        // Test setter with notes exactly 1000 characters (should be valid)
        String validLongNotes = "a".repeat(1000);
        appointment.setNotes(validLongNotes);
        assertThat(appointment.getNotes()).isEqualTo(validLongNotes);
    }

    @Test
    void testCustomDataField() {
        Appointment appointment = new Appointment();

        // Test initial state
        assertThat(appointment.getCustomData()).isNull();

        // Test setter with null
        appointment.setCustomData(null);
        assertThat(appointment.getCustomData()).isNull();

        // Note: JsonNode creation and validation should be handled at service layer
        // Entity only accepts pre-validated JsonNode objects
    }

    // ==================== VALIDATION METHOD TESTS ====================

    @Test
    void testIsScheduledInFutureMethod() {
        Appointment appointment = new Appointment();

        // Test with null scheduledAt
        assertThat(appointment.isScheduledInFuture()).isFalse();

        // Test with future time (valid - more than 1 day in advance)
        appointment.setScheduledAt(OffsetDateTime.now().plusDays(2));
        assertThat(appointment.isScheduledInFuture()).isTrue();

        // Test with past time - need to use reflection or create appointment with past time
        // Since setScheduledAt now validates, we'll test the method logic differently
        // Create appointment with past time using constructor (which doesn't validate)
        Appointment pastAppointment = new Appointment();
        // We can't set past time directly due to validation, so we'll test the method logic
        // by checking what happens when scheduledAt is null vs future
        assertThat(pastAppointment.isScheduledInFuture()).isFalse();
    }

    @Test
    void testHasPatientMethod() {
        Appointment appointment = new Appointment();

        // Test with null patientId
        assertThat(appointment.hasPatient()).isFalse();

        // Test with patientId
        appointment.setPatientId(testPatientId);
        assertThat(appointment.hasPatient()).isTrue();

        // Test with null again
        appointment.setPatientId(null);
        assertThat(appointment.hasPatient()).isFalse();
    }


    @Test
    void testIsReadyForMedicalRecordsMethod() {
        Appointment appointment = new Appointment();

        // Test with no patient
        assertThat(appointment.isReadyForMedicalRecords()).isFalse();

        // Test with patient but not in valid status
        appointment.setPatientId(testPatientId);
        appointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(appointment.isReadyForMedicalRecords()).isFalse();

        // Test with patient and IN_PROGRESS status
        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        assertThat(appointment.isReadyForMedicalRecords()).isTrue();

        // Test with patient and COMPLETED status
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(appointment.isReadyForMedicalRecords()).isTrue();

        // Test with no patient but valid status
        appointment.setPatientId(null);
        assertThat(appointment.isReadyForMedicalRecords()).isFalse();
    }

    @Test
    void testHasExpiredMethod() {
        Appointment appointment = new Appointment();

        // Test with null scheduledAt
        assertThat(appointment.hasExpired()).isFalse();

        // Test with future time and AVAILABLE status (should not expire)
        appointment.setScheduledAt(OffsetDateTime.now().plusDays(2));
        appointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(appointment.hasExpired()).isFalse();

        // Test with future time and SCHEDULED status (should not expire)
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(appointment.hasExpired()).isFalse();

        // Test with future time and COMPLETED status (should not expire)
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(appointment.hasExpired()).isFalse();

        // Note: We can't test with past times due to validation in setScheduledAt
        // The hasExpired method checks if scheduledAt is in the past and status is AVAILABLE or SCHEDULED
        // To test this properly, we would need to use reflection or create a test-specific constructor

        // Test with future time and CANCELLED status (should not expire)
        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(appointment.hasExpired()).isFalse();
    }

    @Test
    void testIsInvalidForBookingMethod() {
        Appointment appointment = new Appointment();

        // Test with null scheduledAt and AVAILABLE status (should be valid for booking)
        appointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(appointment.isInvalidForBooking()).isFalse();

        // Test with SCHEDULED status (should be invalid for booking) - covers line 234
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(appointment.isInvalidForBooking()).isTrue();

        // Test with COMPLETED status (should be invalid for booking) - covers line 234
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(appointment.isInvalidForBooking()).isTrue();

        // Test with CANCELLED status (should be invalid for booking) - covers line 234
        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(appointment.isInvalidForBooking()).isTrue();

        // Note: We can't test with past times due to validation in setScheduledAt
        // The isInvalidForBooking method checks hasExpired() || specific statuses

        // Note: We can't test with past times due to validation in setScheduledAt
        // The hasExpired method checks if scheduledAt is in the past and status is AVAILABLE or SCHEDULED
    }
}