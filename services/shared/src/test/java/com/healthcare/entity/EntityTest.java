package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.MedicalRecordType;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify entity creation and basic operations
 */
@DataJpaTest
@ActiveProfiles("test")
class EntityTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {SqlInitializationAutoConfiguration.class})
    static class TestConfig {
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void testUserEntity() {
        // Create and save a user
        User user = new User("John", "Doe", "john.doe@example.com", UserRole.PATIENT);
        User savedUser = entityManager.persistAndFlush(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(savedUser.getIsActive()).isTrue();
    }

    @Test
    void testPatientEntity() {
        // Create user first
        User user = new User("Jane", "Smith", "jane.smith@example.com", UserRole.PATIENT);
        User savedUser = entityManager.persistAndFlush(user);

        // Create patient
        Patient patient = new Patient(savedUser);
        patient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        patient.setGender("Female");
        patient.setAddress("123 Main St, City, State");
        patient.setMedicalHistory("No significant medical history");

        Patient savedPatient = entityManager.persistAndFlush(patient);

        assertThat(savedPatient.getId()).isNotNull();
        assertThat(savedPatient.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(savedPatient.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(savedPatient.getGender()).isEqualTo("Female");
    }

    @Test
    void testProviderEntity() {
        // Create user first
        User user = new User("Dr. Bob", "Johnson", "dr.bob@example.com", UserRole.PROVIDER);
        User savedUser = entityManager.persistAndFlush(user);

        // Create provider
        Provider provider = new Provider(savedUser);
        provider.setSpecialty("Cardiology");
        provider.setLicenseNumber("MD123456");
        provider.setYearsOfExperience(10);

        Provider savedProvider = entityManager.persistAndFlush(provider);

        assertThat(savedProvider.getId()).isNotNull();
        assertThat(savedProvider.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(savedProvider.getSpecialty()).isEqualTo("Cardiology");
        assertThat(savedProvider.getLicenseNumber()).isEqualTo("MD123456");
        assertThat(savedProvider.getIsAvailable()).isTrue();
    }

    @Test
    void testAppointmentEntity() {
        // Create user and patient
        User patientUser = new User("Patient", "One", "patient@example.com", UserRole.PATIENT);
        User savedPatientUser = entityManager.persistAndFlush(patientUser);
        Patient patient = new Patient(savedPatientUser);
        Patient savedPatient = entityManager.persistAndFlush(patient);

        // Create user and provider
        User providerUser = new User("Dr. Provider", "Two", "provider@example.com", UserRole.PROVIDER);
        User savedProviderUser = entityManager.persistAndFlush(providerUser);
        Provider provider = new Provider(savedProviderUser);
        Provider savedProvider = entityManager.persistAndFlush(provider);

        // Create appointment
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        Appointment appointment = new Appointment(savedPatient, savedProvider, appointmentTime);
        appointment.setReasonForVisit("Regular checkup");
        appointment.setDurationMinutes(45);

        Appointment savedAppointment = entityManager.persistAndFlush(appointment);

        assertThat(savedAppointment.getId()).isNotNull();
        assertThat(savedAppointment.getPatient().getId()).isEqualTo(savedPatient.getId());
        assertThat(savedAppointment.getProvider().getId()).isEqualTo(savedProvider.getId());
        assertThat(savedAppointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(savedAppointment.getDurationMinutes()).isEqualTo(45);
    }

    @Test
    void testMedicalRecordEntity() {
        // Create user and patient
        User patientUser = new User("Patient", "Three", "patient3@example.com", UserRole.PATIENT);
        User savedPatientUser = entityManager.persistAndFlush(patientUser);
        Patient patient = new Patient(savedPatientUser);
        Patient savedPatient = entityManager.persistAndFlush(patient);

        // Create user and provider
        User providerUser = new User("Dr. Provider", "Three", "provider3@example.com", UserRole.PROVIDER);
        User savedProviderUser = entityManager.persistAndFlush(providerUser);
        Provider provider = new Provider(savedProviderUser);
        Provider savedProvider = entityManager.persistAndFlush(provider);

        // Create medical record
        MedicalRecord record = new MedicalRecord(savedPatient, savedProvider, MedicalRecordType.CONSULTATION, "Initial Consultation");
        record.setDiagnosis("Hypertension");
        record.setTreatment("Lifestyle modifications and medication");
        record.setVitalSigns("BP: 140/90, HR: 72");

        MedicalRecord savedRecord = entityManager.persistAndFlush(record);

        assertThat(savedRecord.getId()).isNotNull();
        assertThat(savedRecord.getPatient().getId()).isEqualTo(savedPatient.getId());
        assertThat(savedRecord.getProvider().getId()).isEqualTo(savedProvider.getId());
        assertThat(savedRecord.getRecordType()).isEqualTo(MedicalRecordType.CONSULTATION);
        assertThat(savedRecord.getTitle()).isEqualTo("Initial Consultation");
        assertThat(savedRecord.getDiagnosis()).isEqualTo("Hypertension");
    }

    @Test
    void testAuditLogEntity() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        AuditLog auditLog = new AuditLog(userId, com.healthcare.enums.AuditAction.CREATE, "Patient", resourceId);
        auditLog.setDescription("Patient record created");
        auditLog.setIpAddress("192.168.1.1");

        AuditLog savedAuditLog = entityManager.persistAndFlush(auditLog);

        assertThat(savedAuditLog.getId()).isNotNull();
        assertThat(savedAuditLog.getUserId()).isEqualTo(userId);
        assertThat(savedAuditLog.getAction()).isEqualTo(com.healthcare.enums.AuditAction.CREATE);
        assertThat(savedAuditLog.getResourceType()).isEqualTo("Patient");
        assertThat(savedAuditLog.getResourceId()).isEqualTo(resourceId);
        assertThat(savedAuditLog.getTimestamp()).isNotNull();
    }
}
