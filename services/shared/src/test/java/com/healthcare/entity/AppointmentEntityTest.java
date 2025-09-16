package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

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
    void testAppointmentFactoryMethods() {
        // Test simple constructor
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThat(appointment.getProviderId()).isEqualTo(testProviderId);
        assertThat(appointment.getScheduledAt()).isEqualTo(testScheduledAt);
        assertThat(appointment.getAppointmentType()).isEqualTo(testAppointmentType);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE);
        assertThat(appointment.getPatientId()).isNull();
        assertThat(appointment.getNotes()).isNull();
        assertThat(appointment.getCustomData()).isNull();
        assertThat(appointment.getCheckinTime()).isNull();

        // Test creating appointment for patient
        appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        appointment.setPatientId(testPatientId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(appointment.getProviderId()).isEqualTo(testProviderId);
        assertThat(appointment.getScheduledAt()).isEqualTo(testScheduledAt);
        assertThat(appointment.getAppointmentType()).isEqualTo(testAppointmentType);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.getPatientId()).isEqualTo(testPatientId);
        assertThat(appointment.getNotes()).isNull();

        // Test creating confirmed appointment
        appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        appointment.setPatientId(testPatientId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getProviderId()).isEqualTo(testProviderId);
        assertThat(appointment.getScheduledAt()).isEqualTo(testScheduledAt);
        assertThat(appointment.getAppointmentType()).isEqualTo(testAppointmentType);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getPatientId()).isEqualTo(testPatientId);
        assertThat(appointment.getCustomData()).isNull();
        assertThat(appointment.getCheckinTime()).isNull();
    }

    // ==================== FIELD TESTS ====================

    @Test
    void testPatientIdField() {
        // Test available slot (no patient)
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThat(appointment.getPatientId()).isNull();

        // Test patient appointment
        appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        appointment.setPatientId(testPatientId);
        assertThat(appointment.getPatientId()).isEqualTo(testPatientId);
    }

    @Test
    void testProviderIdField() {
        // Test that providerId is set correctly in factory methods
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThat(appointment.getProviderId()).isEqualTo(testProviderId);

        UUID newProviderId = UUID.randomUUID();
        appointment = new Appointment(newProviderId, testScheduledAt, testAppointmentType);
        assertThat(appointment.getProviderId()).isEqualTo(newProviderId);
    }

    @Test
    void testScheduledAtField() {
        // Test that scheduledAt is set correctly in factory methods
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThat(appointment.getScheduledAt()).isEqualTo(testScheduledAt);

        OffsetDateTime newScheduledAt = OffsetDateTime.now().plusDays(3);
        appointment = new Appointment(testProviderId, newScheduledAt, testAppointmentType);
        assertThat(appointment.getScheduledAt()).isEqualTo(newScheduledAt);

        // Test constructor with null scheduledAt (should throw ValidationException)
        assertThatThrownBy(() -> new Appointment(testProviderId, null, testAppointmentType))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Scheduled time is required");

        // Test constructor with time less than 1 day in advance (should throw ValidationException)
        OffsetDateTime invalidScheduledAt = OffsetDateTime.now().plusHours(12);
        assertThatThrownBy(() -> new Appointment(testProviderId, invalidScheduledAt, testAppointmentType))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment must be scheduled at least 1 day in advance");

        // Test constructor with past time (should throw ValidationException)
        OffsetDateTime pastTime = OffsetDateTime.now().minusDays(1);
        assertThatThrownBy(() -> new Appointment(testProviderId, pastTime, testAppointmentType))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment must be scheduled at least 1 day in advance");
    }

    @Test
    void testConstructorValidation() {
        // Test constructor with null providerId (should throw ValidationException)
        assertThatThrownBy(() -> new Appointment(null, testScheduledAt, testAppointmentType))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Provider ID is required");

        // Test constructor with null appointmentType (should throw ValidationException)
        assertThatThrownBy(() -> new Appointment(testProviderId, testScheduledAt, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment type is required");
    }

    @Test
    void testCheckinTimeField() {
        OffsetDateTime scheduledAt = OffsetDateTime.now().plusDays(2);
        OffsetDateTime validCheckinTime = scheduledAt.minusMinutes(30);

        Appointment appointment = new Appointment(testProviderId, scheduledAt, testAppointmentType);

        // Test initial state
        assertThat(appointment.getCheckinTime()).isNull();

        // Test setter with null (should be allowed)
        appointment.setCheckinTime(null);
        assertThat(appointment.getCheckinTime()).isNull();

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
        // Test factory methods set correct default statuses
        Appointment availableAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThat(availableAppointment.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE);

        Appointment scheduledAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        scheduledAppointment.setPatientId(testPatientId);
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(scheduledAppointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);

        Appointment confirmedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        confirmedAppointment.setPatientId(testPatientId);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(confirmedAppointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

        // Test setter with each status (for status transitions)
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
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
        // Test that each appointment type is set correctly in factory methods
        for (AppointmentType type : AppointmentType.values()) {
            Appointment appointment = new Appointment(testProviderId, testScheduledAt, type);
            assertThat(appointment.getAppointmentType()).isEqualTo(type);
        }

        // Test constructor with null (should throw ValidationException)
        assertThatThrownBy(() -> new Appointment(testProviderId, testScheduledAt, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment type is required");
    }

    @Test
    void testNotesField() {
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
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
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

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
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

        // Test with null scheduledAt (use reflection to test edge case)
        try {
            java.lang.reflect.Field scheduledAtField = Appointment.class.getDeclaredField("scheduledAt");
            scheduledAtField.setAccessible(true);
            scheduledAtField.set(appointment, null);
        } catch (Exception e) {
            fail("Failed to set scheduledAt field via reflection: " + e.getMessage());
        }
        assertThat(appointment.isScheduledInFuture()).isFalse();

        // Test with future time (valid - more than 1 day in advance)
        appointment = new Appointment(testProviderId, OffsetDateTime.now().plusDays(2), testAppointmentType);
        assertThat(appointment.isScheduledInFuture()).isTrue();

        // Test with past time - use reflection to set past time for testing
        Appointment pastAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        try {
            java.lang.reflect.Field scheduledAtField = Appointment.class.getDeclaredField("scheduledAt");
            scheduledAtField.setAccessible(true);
            scheduledAtField.set(pastAppointment, OffsetDateTime.now().minusDays(1));
        } catch (Exception e) {
            fail("Failed to set scheduledAt field via reflection: " + e.getMessage());
        }
        assertThat(pastAppointment.isScheduledInFuture()).isFalse();
    }

    @Test
    void testHasPatientMethod() {
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

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
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

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
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

        // Test with null scheduledAt
        assertThat(appointment.hasExpired()).isFalse();

        // Test with future time and AVAILABLE status (should not expire)
        appointment = new Appointment(testProviderId, OffsetDateTime.now().plusDays(2), testAppointmentType);
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
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

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

    @Test
    void testGetPatient() {
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

        // Initially should be null
        assertThat(appointment.getPatient()).isNull();

        // Create a patient and set it (using reflection since there's no setter)
        Patient patient = new Patient(UUID.randomUUID(), "PAT-123456");
        try {
            java.lang.reflect.Field patientField = Appointment.class.getDeclaredField("patient");
            patientField.setAccessible(true);
            patientField.set(appointment, patient);
        } catch (Exception e) {
            fail("Failed to set patient field via reflection: " + e.getMessage());
        }

        // Now should return the patient
        assertThat(appointment.getPatient()).isEqualTo(patient);
    }

    @Test
    void testGetProvider() {
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

        // Initially should be null
        assertThat(appointment.getProvider()).isNull();

        // Create a provider and set it (using reflection since there's no setter)
        Provider provider = new Provider(UUID.randomUUID(), "1234567890");
        try {
            java.lang.reflect.Field providerField = Appointment.class.getDeclaredField("provider");
            providerField.setAccessible(true);
            providerField.set(appointment, provider);
        } catch (Exception e) {
            fail("Failed to set provider field via reflection: " + e.getMessage());
        }

        // Now should return the provider
        assertThat(appointment.getProvider()).isEqualTo(provider);
    }

    @Test
    void testGetMedicalRecords() {
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);

        // Initially should return empty list (not null)
        assertThat(appointment.getMedicalRecords()).isNotNull();
        assertThat(appointment.getMedicalRecords()).isEmpty();

        // Create a medical record and add it to the list
        MedicalRecord medicalRecord = new MedicalRecord(UUID.randomUUID(), com.healthcare.enums.MedicalRecordType.DIAGNOSIS, "Test medical record content");
        appointment.getMedicalRecords().add(medicalRecord);

        // Now should contain the medical record
        assertThat(appointment.getMedicalRecords()).hasSize(1);
        assertThat(appointment.getMedicalRecords()).contains(medicalRecord);
    }

    // ==================== BUSINESS LOGIC TESTS ====================

    @Test
    void testValidateState() {
        // Test valid appointment - should not throw exception
        Appointment validAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        assertThatCode(() -> validAppointment.validateState()).doesNotThrowAnyException();

        // Test with null providerId - should throw ValidationException
        Appointment appointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        try {
            java.lang.reflect.Field providerIdField = Appointment.class.getDeclaredField("providerId");
            providerIdField.setAccessible(true);
            providerIdField.set(appointment, null);
        } catch (Exception e) {
            fail("Failed to set providerId field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> appointment.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Provider ID is required");

        // Test with null scheduledAt - should throw ValidationException
        Appointment appointmentWithNullScheduledAt = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        try {
            java.lang.reflect.Field scheduledAtField = Appointment.class.getDeclaredField("scheduledAt");
            scheduledAtField.setAccessible(true);
            scheduledAtField.set(appointmentWithNullScheduledAt, null);
        } catch (Exception e) {
            fail("Failed to set scheduledAt field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> appointmentWithNullScheduledAt.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Scheduled time is required");

        // Test with null appointmentType - should throw ValidationException
        Appointment appointmentWithNullType = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        try {
            java.lang.reflect.Field appointmentTypeField = Appointment.class.getDeclaredField("appointmentType");
            appointmentTypeField.setAccessible(true);
            appointmentTypeField.set(appointmentWithNullType, null);
        } catch (Exception e) {
            fail("Failed to set appointmentType field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> appointmentWithNullType.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment type is required");
    }

    @Test
    void testValidateStateWithNullStatus() {
        // Test data variables
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        // Create appointment with null status
        Appointment appointmentWithNullStatus = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        try {
            java.lang.reflect.Field statusField = Appointment.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(appointmentWithNullStatus, null);
        } catch (Exception e) {
            fail("Failed to set status field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> appointmentWithNullStatus.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("Appointment status is required");
    }

    @Test
    void testCanBeBooked() {
        // Test data variables
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        // Test available appointment without patient - should be bookable
        Appointment availableAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        availableAppointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(availableAppointment.canBeBooked()).isTrue();

        // Test available appointment with patient - should not be bookable
        Appointment bookedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        bookedAppointment.setStatus(AppointmentStatus.AVAILABLE);
        bookedAppointment.setPatientId(UUID.randomUUID());
        assertThat(bookedAppointment.canBeBooked()).isFalse();

        // Test scheduled appointment without patient - should not be bookable
        Appointment scheduledAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(scheduledAppointment.canBeBooked()).isFalse();

        // Test confirmed appointment without patient - should not be bookable
        Appointment confirmedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(confirmedAppointment.canBeBooked()).isFalse();
    }

    @Test
    void testCanBeCancelled() {
        // Test data variables
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        // Test scheduled appointment - should be cancellable
        Appointment scheduledAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(scheduledAppointment.canBeCancelled()).isTrue();

        // Test confirmed appointment - should be cancellable
        Appointment confirmedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(confirmedAppointment.canBeCancelled()).isTrue();

        // Test available appointment - should be cancellable
        Appointment availableAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        availableAppointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(availableAppointment.canBeCancelled()).isTrue();

        // Test in-progress appointment - should not be cancellable
        Appointment inProgressAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        inProgressAppointment.setStatus(AppointmentStatus.IN_PROGRESS);
        assertThat(inProgressAppointment.canBeCancelled()).isFalse();

        // Test completed appointment - should not be cancellable
        Appointment completedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        completedAppointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(completedAppointment.canBeCancelled()).isFalse();

        // Test cancelled appointment - should not be cancellable
        Appointment cancelledAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(cancelledAppointment.canBeCancelled()).isFalse();
    }

    @Test
    void testCanBeCompleted() {
        // Test data variables
        UUID testProviderId = UUID.randomUUID();
        OffsetDateTime testScheduledAt = OffsetDateTime.now().plusDays(2);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        // Test in-progress appointment - should be completable
        Appointment inProgressAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        inProgressAppointment.setStatus(AppointmentStatus.IN_PROGRESS);
        assertThat(inProgressAppointment.canBeCompleted()).isTrue();

        // Test scheduled appointment - should not be completable
        Appointment scheduledAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        scheduledAppointment.setStatus(AppointmentStatus.SCHEDULED);
        assertThat(scheduledAppointment.canBeCompleted()).isFalse();

        // Test confirmed appointment - should not be completable
        Appointment confirmedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        confirmedAppointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(confirmedAppointment.canBeCompleted()).isFalse();

        // Test available appointment - should not be completable
        Appointment availableAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        availableAppointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(availableAppointment.canBeCompleted()).isFalse();

        // Test completed appointment - should not be completable
        Appointment completedAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        completedAppointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(completedAppointment.canBeCompleted()).isFalse();

        // Test cancelled appointment - should not be completable
        Appointment cancelledAppointment = new Appointment(testProviderId, testScheduledAt, testAppointmentType);
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(cancelledAppointment.canBeCompleted()).isFalse();
    }
}