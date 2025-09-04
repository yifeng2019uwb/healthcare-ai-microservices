package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.AppointmentType;
import com.healthcare.enums.Gender;
import com.healthcare.enums.MedicalRecordType;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.ResourceType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for entity classes - testing entity creation and basic operations
 * without Spring context or database dependencies
 */
class EntityTest {

    @Test
    void testUserEntity() {
        // Create a user using the correct constructor
        User user = new User("ext-auth-123", "John", "Doe", "john.doe@example.com",
                           "+1234567890", LocalDate.of(1990, 5, 15), Gender.FEMALE, UserRole.PATIENT);

        // Test basic properties
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();

        // Test setters
        user.setPhone("+1234567890");
        assertThat(user.getPhone()).isEqualTo("+1234567890");

        user.setDateOfBirth(LocalDate.of(1990, 5, 15));
        assertThat(user.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));

        user.setGender(Gender.FEMALE);
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);

        user.setStreetAddress("123 Main St");
        user.setCity("City");
        user.setState("State");
        user.setPostalCode("12345");
        user.setCountry("USA");
        assertThat(user.getStreetAddress()).isEqualTo("123 Main St");
        assertThat(user.getCity()).isEqualTo("City");
        assertThat(user.getState()).isEqualTo("State");
        assertThat(user.getPostalCode()).isEqualTo("12345");
        assertThat(user.getCountry()).isEqualTo("USA");

        user.setStatus(UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void testPatientEntity() {
        // Create user first
        User user = new User("ext-auth-456", "Jane", "Smith", "jane.smith@example.com",
                           "+1234567890", LocalDate.of(1990, 5, 15), Gender.FEMALE, UserRole.PATIENT);

        // Create patient
        Patient patient = new Patient(user, "PAT-12345678");
        patient.setMedicalHistory("No significant medical history");

        // Test basic properties
        assertThat(patient.getUser()).isEqualTo(user);
        assertThat(patient.getPatientNumber()).isEqualTo("PAT-12345678");
        assertThat(patient.getMedicalHistory()).isEqualTo("No significant medical history");

        // Test additional setters
        patient.setAllergies("Peanuts");
        assertThat(patient.getAllergies()).isEqualTo("Peanuts");

        patient.setEmergencyContactName("John Smith");
        patient.setEmergencyContactPhone("+15551234567");
        assertThat(patient.getEmergencyContactName()).isEqualTo("John Smith");
        assertThat(patient.getEmergencyContactPhone()).isEqualTo("+15551234567");
    }

    @Test
    void testProviderEntity() {
        // Create user first
        User user = new User("ext-auth-789", "Dr. Bob", "Johnson", "dr.bob@example.com",
                           "+1234567890", LocalDate.of(1980, 3, 20), Gender.MALE, UserRole.PROVIDER);

        // Create provider
        Provider provider = new Provider(user, "1234567890");
        provider.setSpecialty("Cardiology");
        provider.setLicenseNumbers("MD123456");
        provider.setQualifications("MD, PhD in Cardiology");

        // Test basic properties
        assertThat(provider.getUser()).isEqualTo(user);
        assertThat(provider.getNpiNumber()).isEqualTo("1234567890");
        assertThat(provider.getSpecialty()).isEqualTo("Cardiology");
        assertThat(provider.getLicenseNumbers()).isEqualTo("MD123456");
        assertThat(provider.getQualifications()).isEqualTo("MD, PhD in Cardiology");

        // Test additional setters
        provider.setBio("Experienced cardiologist");
        provider.setOfficePhone("+15555678901");
        provider.setCustomData("{\"rating\": 4.8}");

        assertThat(provider.getBio()).isEqualTo("Experienced cardiologist");
        assertThat(provider.getOfficePhone()).isEqualTo("+15555678901");
        assertThat(provider.getCustomData()).isEqualTo("{\"rating\": 4.8}");
    }

    @Test
    void testAppointmentEntity() {
        // Create user and patient
        User patientUser = new User("ext-auth-patient", "Patient", "One", "patient@example.com",
                                  "+1234567890", LocalDate.of(1990, 1, 1), Gender.FEMALE, UserRole.PATIENT);
        Patient patient = new Patient(patientUser, "PAT-87654321");

        // Create user and provider
        User providerUser = new User("ext-auth-provider", "Dr. Provider", "Two", "provider@example.com",
                                   "+1234567890", LocalDate.of(1980, 1, 1), Gender.MALE, UserRole.PROVIDER);
        Provider provider = new Provider(providerUser, "9876543210");

        // Create appointment
        OffsetDateTime appointmentTime = OffsetDateTime.now().plusDays(1);
        Appointment appointment = new Appointment(patient, provider, appointmentTime, AppointmentType.REGULAR_CONSULTATION);
        appointment.setNotes("Regular checkup");

        // Test basic properties
        assertThat(appointment.getPatient()).isEqualTo(patient);
        assertThat(appointment.getProvider()).isEqualTo(provider);
        assertThat(appointment.getScheduledAt()).isEqualTo(appointmentTime);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.getAppointmentType()).isEqualTo(AppointmentType.REGULAR_CONSULTATION);
        assertThat(appointment.getNotes()).isEqualTo("Regular checkup");

        // Test additional setters
        appointment.setNotes("Patient has allergies");
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setCheckinTime(OffsetDateTime.now());

        assertThat(appointment.getNotes()).isEqualTo("Patient has allergies");
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getCheckinTime()).isNotNull();
    }

    @Test
    void testMedicalRecordEntity() {
        // Create user and patient
        User patientUser = new User("ext-auth-patient3", "Patient", "Three", "patient3@example.com",
                                  "+1234567890", LocalDate.of(1990, 1, 1), Gender.FEMALE, UserRole.PATIENT);
        Patient patient = new Patient(patientUser, "PAT-11111111");

        // Create user and provider
        User providerUser = new User("ext-auth-provider3", "Dr. Provider", "Three", "provider3@example.com",
                                   "+1234567890", LocalDate.of(1980, 1, 1), Gender.MALE, UserRole.PROVIDER);
        Provider provider = new Provider(providerUser, "1111111111");

        // Create appointment first
        OffsetDateTime appointmentTime = OffsetDateTime.now().plusDays(1);
        Appointment appointment = new Appointment(patient, provider, appointmentTime, AppointmentType.REGULAR_CONSULTATION);

        // Create medical record
        MedicalRecord record = new MedicalRecord(appointment, MedicalRecordType.DIAGNOSIS, "Initial Consultation - Hypertension diagnosis");
        record.setIsPatientVisible(true);

        // Test basic properties
        assertThat(record.getAppointment()).isEqualTo(appointment);
        assertThat(record.getRecordType()).isEqualTo(MedicalRecordType.DIAGNOSIS);
        assertThat(record.getContent()).isEqualTo("Initial Consultation - Hypertension diagnosis");
        assertThat(record.getIsPatientVisible()).isTrue();

        // Test additional setters
        record.setContent("Updated diagnosis: Hypertension stage 1");
        record.setReleaseDate(OffsetDateTime.now().plusDays(1));
        record.setCustomData("{\"priority\": \"high\"}");

        assertThat(record.getContent()).isEqualTo("Updated diagnosis: Hypertension stage 1");
        assertThat(record.getReleaseDate()).isNotNull();
        assertThat(record.getCustomData()).isEqualTo("{\"priority\": \"high\"}");
    }

    @Test
    void testAuditLogEntity() {
        // Create a user first
        User user = new User("ext-auth-audit", "Audit", "User", "audit@example.com",
                           "+1234567890", LocalDate.of(1990, 1, 1), Gender.OTHER, UserRole.PATIENT);

        UUID resourceId = UUID.randomUUID();

        AuditLog auditLog = new AuditLog(user, ActionType.CREATE, ResourceType.PATIENT_PROFILE, resourceId, Outcome.SUCCESS);
        auditLog.setDetails("{\"action\": \"Patient record created\"}");
        auditLog.setUserAgent("Mozilla/5.0");

        // Test basic properties
        assertThat(auditLog.getUser()).isEqualTo(user);
        assertThat(auditLog.getActionType()).isEqualTo(ActionType.CREATE);
        assertThat(auditLog.getResourceType()).isEqualTo(ResourceType.PATIENT_PROFILE);
        assertThat(auditLog.getResourceId()).isEqualTo(resourceId);
        assertThat(auditLog.getOutcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(auditLog.getDetails()).isEqualTo("{\"action\": \"Patient record created\"}");
        assertThat(auditLog.getUserAgent()).isEqualTo("Mozilla/5.0");

        // Test additional setters
        auditLog.setDetails("{\"action\": \"Patient record updated\"}");
        auditLog.setOutcome(Outcome.FAILURE);

        assertThat(auditLog.getDetails()).isEqualTo("{\"action\": \"Patient record updated\"}");
        assertThat(auditLog.getOutcome()).isEqualTo(Outcome.FAILURE);
    }
}
