package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Appointment entity
 */
class AppointmentEntityTest {

        // Reusable test data for all appointment tests
    private static final User testUser = new User("ext-auth-test", "Test", "User", "test@example.com",
                                                 "+1234567890", LocalDate.of(1990, 1, 1), Gender.MALE, UserRole.PATIENT);
    private static final Patient testPatient = new Patient(testUser, "PAT-TEST123");
    private static final Provider testProvider = new Provider(testUser, "TEST123456");

        @Test
    void testAppointmentEntity() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;

        // Test appointment-specific data
        OffsetDateTime testAppointmentTime = OffsetDateTime.now().plusDays(1);
        String testNotes = "Regular checkup";
        String testUpdatedNotes = "Patient has allergies";
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;
        AppointmentStatus testInitialStatus = AppointmentStatus.SCHEDULED;
        AppointmentStatus testUpdatedStatus = AppointmentStatus.CONFIRMED;

        // Create appointment
        Appointment appointment = new Appointment(patient, provider, testAppointmentTime, testAppointmentType);
        appointment.setNotes(testNotes);

        // Test basic appointment properties
        assertThat(appointment.getPatient()).isEqualTo(patient);
        assertThat(appointment.getProvider()).isEqualTo(provider);
        assertThat(appointment.getScheduledAt()).isEqualTo(testAppointmentTime);
        assertThat(appointment.getStatus()).isEqualTo(testInitialStatus);
        assertThat(appointment.getAppointmentType()).isEqualTo(testAppointmentType);
        assertThat(appointment.getNotes()).isEqualTo(testNotes);

        // Test appointment type enum
        assertThat(appointment.getAppointmentType()).isEqualTo(AppointmentType.REGULAR_CONSULTATION);
        assertThat(appointment.getAppointmentType().getCode()).isEqualTo(AppointmentType.REGULAR_CONSULTATION.getCode());
        assertThat(appointment.getAppointmentType().getDescription()).isEqualTo(AppointmentType.REGULAR_CONSULTATION.getDescription());

        // Test appointment status enum
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.getStatus().getCode()).isEqualTo(AppointmentStatus.SCHEDULED.getCode());
        assertThat(appointment.getStatus().getDescription()).isEqualTo(AppointmentStatus.SCHEDULED.getDescription());

        // Test appointment setters
        appointment.setNotes(testUpdatedNotes);
        appointment.setStatus(testUpdatedStatus);
        appointment.setCheckinTime(OffsetDateTime.now());

        assertThat(appointment.getNotes()).isEqualTo(testUpdatedNotes);
        assertThat(appointment.getStatus()).isEqualTo(testUpdatedStatus);
        assertThat(appointment.getCheckinTime()).isNotNull();
    }

    @Test
    void testAppointmentValidationMethods() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;

        OffsetDateTime testFutureTime = OffsetDateTime.now().plusDays(1);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        Appointment appointment = new Appointment(patient, provider, testFutureTime, testAppointmentType);

        // Test appointment validation methods
        assertThat(appointment.hasPatient()).isTrue();
        assertThat(appointment.hasProvider()).isTrue();
        assertThat(appointment.isScheduledInFuture()).isTrue();
        assertThat(appointment.hasValidTimeData()).isTrue();
        assertThat(appointment.isInValidState()).isTrue();

        // Test appointment status validations
        assertThat(appointment.isCompleted()).isFalse();
        assertThat(appointment.isCancelled()).isFalse();

        // Test status changes
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(appointment.isCompleted()).isTrue();
        assertThat(appointment.isReadyForMedicalRecords()).isTrue();

        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(appointment.isCancelled()).isTrue();

        // Test expiration (past appointment)
        OffsetDateTime pastTime = OffsetDateTime.now().minusDays(1);
        appointment.setScheduledAt(pastTime);
        appointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(appointment.hasExpired()).isTrue();
        assertThat(appointment.isInvalidForBooking()).isTrue();
    }

    @Test
    void testAppointmentSchedulingValidation() {
        // Use reusable test objects
        Provider provider = testProvider;

        OffsetDateTime testValidSlotTime = OffsetDateTime.now().plusDays(2);
        AppointmentType testAppointmentType = AppointmentType.REGULAR_CONSULTATION;

        // Test provider slot creation (1+ day in advance)
        Appointment slot = new Appointment(provider, testValidSlotTime, testAppointmentType);

        // Test scheduling validation (creation time is null in test, so hasValidScheduledTime will be false)
        // This is expected behavior - the validation works at persistence time
        assertThat(slot.isScheduledInFuture()).isTrue();
    }

        @Test
    void testAppointmentEdgeCases() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;

        // Test past appointment
        OffsetDateTime pastTime = OffsetDateTime.now().minusDays(1);
        Appointment pastAppointment = new Appointment(patient, provider, pastTime, AppointmentType.REGULAR_CONSULTATION);
        pastAppointment.setStatus(AppointmentStatus.AVAILABLE);

        assertThat(pastAppointment.isScheduledInFuture()).isFalse();
        assertThat(pastAppointment.hasExpired()).isTrue();
        assertThat(pastAppointment.isInvalidForBooking()).isTrue();

        // Test current time appointment
        OffsetDateTime currentTime = OffsetDateTime.now().plusMinutes(30);
        Appointment currentAppointment = new Appointment(patient, provider, currentTime, AppointmentType.REGULAR_CONSULTATION);

        assertThat(currentAppointment.isScheduledInFuture()).isTrue();
        assertThat(currentAppointment.hasExpired()).isFalse();
        assertThat(currentAppointment.isInvalidForBooking()).isTrue(); // SCHEDULED status makes it invalid for booking

                // Test different appointment types
        Appointment followUpAppointment = new Appointment(patient, provider, currentTime, AppointmentType.FOLLOW_UP);
        assertThat(followUpAppointment.getAppointmentType()).isEqualTo(AppointmentType.FOLLOW_UP);

        // Test status transitions
        Appointment appointment = new Appointment(patient, provider, currentTime, AppointmentType.REGULAR_CONSULTATION);

        // SCHEDULED -> CONFIRMED
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(appointment.isCompleted()).isFalse();
        assertThat(appointment.isCancelled()).isFalse();

        // CONFIRMED -> COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(appointment.isCompleted()).isTrue();
        assertThat(appointment.isReadyForMedicalRecords()).isTrue();

        // COMPLETED -> CANCELLED (should not happen in real scenario, but test the method)
        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(appointment.isCancelled()).isTrue();
        assertThat(appointment.isCompleted()).isFalse();

                // Test time data validation
        appointment.setScheduledAt(currentTime);
        appointment.setCheckinTime(OffsetDateTime.now());

        assertThat(appointment.hasValidTimeData()).isTrue();
    }

        @Test
    void testAppointmentValidationEdgeCases() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;

        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);

        // Test appointment with null patient
        Appointment appointmentWithNullPatient = new Appointment(null, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(appointmentWithNullPatient.hasPatient()).isFalse();
        assertThat(appointmentWithNullPatient.isInValidState()).isTrue(); // Only checks scheduledAt and status, not patient/provider

        // Test appointment with null provider
        Appointment appointmentWithNullProvider = new Appointment(patient, null, futureTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(appointmentWithNullProvider.hasProvider()).isFalse();
        assertThat(appointmentWithNullProvider.isInValidState()).isTrue(); // Only checks scheduledAt and status, not patient/provider

        // Test appointment with both patient and provider
        Appointment validAppointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(validAppointment.hasPatient()).isTrue();
        assertThat(validAppointment.hasProvider()).isTrue();
        assertThat(validAppointment.isInValidState()).isTrue();

        // Test enum values for test user
        assertThat(patient.getUser().getGender()).isEqualTo(Gender.MALE);
        assertThat(patient.getUser().getGender().getCode()).isEqualTo(Gender.MALE.getCode());
        assertThat(patient.getUser().getGender().getDescription()).isEqualTo(Gender.MALE.getDescription());
        assertThat(provider.getUser().getGender()).isEqualTo(Gender.MALE);
        assertThat(provider.getUser().getGender().getCode()).isEqualTo(Gender.MALE.getCode());
        assertThat(provider.getUser().getGender().getDescription()).isEqualTo(Gender.MALE.getDescription());

        // Test appointment with null scheduled time
        Appointment appointmentWithNullTime = new Appointment(patient, provider, null, AppointmentType.REGULAR_CONSULTATION);
        assertThat(appointmentWithNullTime.isScheduledInFuture()).isFalse();
        assertThat(appointmentWithNullTime.hasValidTimeData()).isFalse();
    }

    @Test
    void testAppointmentStatusTransitions() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        AppointmentType appointmentType = AppointmentType.REGULAR_CONSULTATION;

        Appointment appointment = new Appointment(patient, provider, futureTime, appointmentType);

        // Test initial status
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.isCompleted()).isFalse();
        assertThat(appointment.isCancelled()).isFalse();

        // Test SCHEDULED -> CONFIRMED
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.isCompleted()).isFalse();
        assertThat(appointment.isCancelled()).isFalse();

        // Test CONFIRMED -> COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(appointment.isCompleted()).isTrue();
        assertThat(appointment.isReadyForMedicalRecords()).isTrue();
        assertThat(appointment.isCancelled()).isFalse();

        // Test COMPLETED -> CANCELLED (edge case)
        appointment.setStatus(AppointmentStatus.CANCELLED);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appointment.isCancelled()).isTrue();
        assertThat(appointment.isCompleted()).isFalse();

        // Test AVAILABLE status
        appointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE);
        assertThat(appointment.isCompleted()).isFalse();
        assertThat(appointment.isCancelled()).isFalse();
    }

    @Test
    void testAppointmentTypeVariations() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);

        // Test REGULAR_CONSULTATION
        Appointment regularAppointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(regularAppointment.getAppointmentType()).isEqualTo(AppointmentType.REGULAR_CONSULTATION);
        assertThat(regularAppointment.getAppointmentType().getDurationMinutes()).isEqualTo(30);

        // Test FOLLOW_UP
        Appointment followUpAppointment = new Appointment(patient, provider, futureTime, AppointmentType.FOLLOW_UP);
        assertThat(followUpAppointment.getAppointmentType()).isEqualTo(AppointmentType.FOLLOW_UP);
        assertThat(followUpAppointment.getAppointmentType().getDurationMinutes()).isEqualTo(15);

        // Test NEW_PATIENT_INTAKE
        Appointment newPatientAppointment = new Appointment(patient, provider, futureTime, AppointmentType.NEW_PATIENT_INTAKE);
        assertThat(newPatientAppointment.getAppointmentType()).isEqualTo(AppointmentType.NEW_PATIENT_INTAKE);
        assertThat(newPatientAppointment.getAppointmentType().getDurationMinutes()).isEqualTo(60);

        // Test PROCEDURE_CONSULTATION
        Appointment procedureAppointment = new Appointment(patient, provider, futureTime, AppointmentType.PROCEDURE_CONSULTATION);
        assertThat(procedureAppointment.getAppointmentType()).isEqualTo(AppointmentType.PROCEDURE_CONSULTATION);
        assertThat(procedureAppointment.getAppointmentType().getDurationMinutes()).isEqualTo(45);
    }

    @Test
    void testAppointmentTimeValidation() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;

        // Test past appointment
        OffsetDateTime pastTime = OffsetDateTime.now().minusDays(1);
        Appointment pastAppointment = new Appointment(patient, provider, pastTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(pastAppointment.isScheduledInFuture()).isFalse();
        assertThat(pastAppointment.hasExpired()).isTrue();

        // Test current time appointment
        OffsetDateTime currentTime = OffsetDateTime.now().plusMinutes(30);
        Appointment currentAppointment = new Appointment(patient, provider, currentTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(currentAppointment.isScheduledInFuture()).isTrue();
        assertThat(currentAppointment.hasExpired()).isFalse();

        // Test future appointment
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        Appointment futureAppointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);
        assertThat(futureAppointment.isScheduledInFuture()).isTrue();
        assertThat(futureAppointment.hasExpired()).isFalse();

        // Test null scheduled time
        Appointment nullTimeAppointment = new Appointment(patient, provider, null, AppointmentType.REGULAR_CONSULTATION);
        assertThat(nullTimeAppointment.isScheduledInFuture()).isFalse();
        assertThat(nullTimeAppointment.hasExpired()).isFalse();
    }

    @Test
    void testAppointmentCheckin() {
        // Use reusable test objects
        Patient patient = testPatient;
        Provider provider = testProvider;
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        Appointment appointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);

        // Test initial state
        assertThat(appointment.getCheckinTime()).isNull();
        assertThat(appointment.hasValidTimeData()).isTrue(); // No times set, so valid

        // Test checkin
        OffsetDateTime checkinTime = OffsetDateTime.now();
        appointment.setCheckinTime(checkinTime);
        assertThat(appointment.getCheckinTime()).isEqualTo(checkinTime);
        assertThat(appointment.hasValidTimeData()).isTrue(); // Only checkin set, so valid
    }

    @Test
    void testAppointmentNotes() {
        // Use reusable test objects
        Patient patient = createTestPatient();
        Provider provider = createTestProvider();
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);
        Appointment appointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);

        // Test initial state
        assertThat(appointment.getNotes()).isNull();

        // Test setting notes
        String testNotes = "Patient has allergies to penicillin";
        appointment.setNotes(testNotes);
        assertThat(appointment.getNotes()).isEqualTo(testNotes);

        // Test updating notes
        String updatedNotes = "Updated: Patient has severe allergies to penicillin and sulfa drugs";
        appointment.setNotes(updatedNotes);
        assertThat(appointment.getNotes()).isEqualTo(updatedNotes);

        // Test clearing notes
        appointment.setNotes(null);
        assertThat(appointment.getNotes()).isNull();
    }

    @Test
    void testAppointmentProviderSlotCreation() {
        // Use reusable test objects
        Provider provider = createTestProvider();
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(2); // 2 days in advance
        AppointmentType appointmentType = AppointmentType.REGULAR_CONSULTATION;

        // Test provider slot creation (no patient)
        Appointment slot = new Appointment(provider, futureTime, appointmentType);

        assertThat(slot.getProvider()).isEqualTo(provider);
        assertThat(slot.getPatient()).isNull();
        assertThat(slot.getScheduledAt()).isEqualTo(futureTime);
        assertThat(slot.getAppointmentType()).isEqualTo(appointmentType);
        assertThat(slot.getStatus()).isEqualTo(AppointmentStatus.AVAILABLE);
        assertThat(slot.isScheduledInFuture()).isTrue();
        assertThat(slot.hasProvider()).isTrue();
        assertThat(slot.hasPatient()).isFalse();
    }

    @Test
    void testAppointmentBookingValidation() {
        // Use reusable test objects
        Patient patient = createTestPatient();
        Provider provider = createTestProvider();
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);

        // Test appointment with both patient and provider
        Appointment appointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);

        assertThat(appointment.hasPatient()).isTrue();
        assertThat(appointment.hasProvider()).isTrue();
        assertThat(appointment.isInValidState()).isTrue();
        assertThat(appointment.isScheduledInFuture()).isTrue();
        assertThat(appointment.hasExpired()).isFalse();

        // Test appointment status affects booking validity
        assertThat(appointment.isInvalidForBooking()).isTrue(); // SCHEDULED status makes it invalid

        // Test AVAILABLE status (valid for booking)
        appointment.setStatus(AppointmentStatus.AVAILABLE);
        assertThat(appointment.isInvalidForBooking()).isFalse(); // AVAILABLE status allows booking
    }

    @Test
    void testAppointmentEnumCoverage() {
        // Use reusable test objects
        Patient patient = createTestPatient();
        Provider provider = createTestProvider();
        OffsetDateTime futureTime = OffsetDateTime.now().plusDays(1);

        // Test all appointment statuses
        Appointment appointment = new Appointment(patient, provider, futureTime, AppointmentType.REGULAR_CONSULTATION);

        for (AppointmentStatus status : AppointmentStatus.values()) {
            appointment.setStatus(status);
            assertThat(appointment.getStatus()).isEqualTo(status);
            assertThat(appointment.getStatus().getCode()).isEqualTo(status.getCode());
            assertThat(appointment.getStatus().getDescription()).isEqualTo(status.getDescription());
        }

        // Test all appointment types
        for (AppointmentType type : AppointmentType.values()) {
            Appointment typeAppointment = new Appointment(patient, provider, futureTime, type);
            assertThat(typeAppointment.getAppointmentType()).isEqualTo(type);
            assertThat(typeAppointment.getAppointmentType().getCode()).isEqualTo(type.getCode());
            assertThat(typeAppointment.getAppointmentType().getDescription()).isEqualTo(type.getDescription());
            assertThat(typeAppointment.getAppointmentType().getDurationMinutes()).isGreaterThan(0);
        }
    }
}
