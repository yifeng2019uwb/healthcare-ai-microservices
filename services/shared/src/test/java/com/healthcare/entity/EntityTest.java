package com.healthcare.entity;

import com.healthcare.enums.AppointmentStatus;
import com.healthcare.enums.MedicalRecordType;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for entity classes - testing entity creation and basic operations
 * without Spring context or database dependencies
 */
class EntityTest {

    @Test
    void testUserEntity() {
        // Create a user
        User user = new User("John", "Doe", "john.doe@example.com", UserRole.PATIENT);

        // Test basic properties
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(user.getIsActive()).isTrue();

        // Test setters
        user.setPhone("123-456-7890");
        assertThat(user.getPhone()).isEqualTo("123-456-7890");

        user.setIsActive(false);
        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    void testPatientEntity() {
        // Create user first
        User user = new User("Jane", "Smith", "jane.smith@example.com", UserRole.PATIENT);

        // Create patient
        Patient patient = new Patient(user);
        patient.setDateOfBirth(LocalDate.of(1990, 5, 15));
        patient.setGender("Female");
        patient.setAddress("123 Main St, City, State");
        patient.setMedicalHistory("No significant medical history");

        // Test basic properties
        assertThat(patient.getUser()).isEqualTo(user);
        assertThat(patient.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(patient.getGender()).isEqualTo("Female");
        assertThat(patient.getAddress()).isEqualTo("123 Main St, City, State");
        assertThat(patient.getMedicalHistory()).isEqualTo("No significant medical history");

        // Test additional setters
        patient.setAllergies("Peanuts");
        patient.setEmergencyContactName("John Smith");
        patient.setEmergencyContactPhone("555-1234");

        assertThat(patient.getAllergies()).isEqualTo("Peanuts");
        assertThat(patient.getEmergencyContactName()).isEqualTo("John Smith");
        assertThat(patient.getEmergencyContactPhone()).isEqualTo("555-1234");
    }

    @Test
    void testProviderEntity() {
        // Create user first
        User user = new User("Dr. Bob", "Johnson", "dr.bob@example.com", UserRole.PROVIDER);

        // Create provider
        Provider provider = new Provider(user);
        provider.setSpecialty("Cardiology");
        provider.setLicenseNumber("MD123456");
        provider.setYearsOfExperience(10);

        // Test basic properties
        assertThat(provider.getUser()).isEqualTo(user);
        assertThat(provider.getSpecialty()).isEqualTo("Cardiology");
        assertThat(provider.getLicenseNumber()).isEqualTo("MD123456");
        assertThat(provider.getYearsOfExperience()).isEqualTo(10);
        assertThat(provider.getIsAvailable()).isTrue();

        // Test additional setters
        provider.setBio("Experienced cardiologist");
        provider.setOfficeAddress("123 Medical Center");
        provider.setOfficePhone("555-5678");
        provider.setQualification("MD, PhD");
        provider.setIsAvailable(false);

        assertThat(provider.getBio()).isEqualTo("Experienced cardiologist");
        assertThat(provider.getOfficeAddress()).isEqualTo("123 Medical Center");
        assertThat(provider.getOfficePhone()).isEqualTo("555-5678");
        assertThat(provider.getQualification()).isEqualTo("MD, PhD");
        assertThat(provider.getIsAvailable()).isFalse();
    }

    @Test
    void testAppointmentEntity() {
        // Create user and patient
        User patientUser = new User("Patient", "One", "patient@example.com", UserRole.PATIENT);
        Patient patient = new Patient(patientUser);

        // Create user and provider
        User providerUser = new User("Dr. Provider", "Two", "provider@example.com", UserRole.PROVIDER);
        Provider provider = new Provider(providerUser);

        // Create appointment
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1);
        Appointment appointment = new Appointment(patient, provider, appointmentTime);
        appointment.setReasonForVisit("Regular checkup");
        appointment.setDurationMinutes(45);

        // Test basic properties
        assertThat(appointment.getPatient()).isEqualTo(patient);
        assertThat(appointment.getProvider()).isEqualTo(provider);
        assertThat(appointment.getAppointmentDate()).isEqualTo(appointmentTime);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(appointment.getDurationMinutes()).isEqualTo(45);
        assertThat(appointment.getReasonForVisit()).isEqualTo("Regular checkup");

        // Test additional setters
        appointment.setNotes("Patient has allergies");
        appointment.setIsUrgent(true);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        assertThat(appointment.getNotes()).isEqualTo("Patient has allergies");
        assertThat(appointment.getIsUrgent()).isTrue();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void testMedicalRecordEntity() {
        // Create user and patient
        User patientUser = new User("Patient", "Three", "patient3@example.com", UserRole.PATIENT);
        Patient patient = new Patient(patientUser);

        // Create user and provider
        User providerUser = new User("Dr. Provider", "Three", "provider3@example.com", UserRole.PROVIDER);
        Provider provider = new Provider(providerUser);

        // Create medical record
        MedicalRecord record = new MedicalRecord(patient, provider, MedicalRecordType.CONSULTATION, "Initial Consultation");
        record.setDiagnosis("Hypertension");
        record.setTreatment("Lifestyle modifications and medication");
        record.setVitalSigns("BP: 140/90, HR: 72");

        // Test basic properties
        assertThat(record.getPatient()).isEqualTo(patient);
        assertThat(record.getProvider()).isEqualTo(provider);
        assertThat(record.getRecordType()).isEqualTo(MedicalRecordType.CONSULTATION);
        assertThat(record.getTitle()).isEqualTo("Initial Consultation");
        assertThat(record.getDiagnosis()).isEqualTo("Hypertension");
        assertThat(record.getTreatment()).isEqualTo("Lifestyle modifications and medication");
        assertThat(record.getVitalSigns()).isEqualTo("BP: 140/90, HR: 72");

        // Test additional setters
        record.setDescription("Follow-up consultation");
        record.setMedications("Lisinopril 10mg daily");
        record.setFileUrls("[\"https://example.com/lab-results.pdf\"]");

        assertThat(record.getDescription()).isEqualTo("Follow-up consultation");
        assertThat(record.getMedications()).isEqualTo("Lisinopril 10mg daily");
        assertThat(record.getFileUrls()).isEqualTo("[\"https://example.com/lab-results.pdf\"]");
    }

    @Test
    void testAuditLogEntity() {
        UUID userId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        AuditLog auditLog = new AuditLog(userId, com.healthcare.enums.AuditAction.CREATE, "Patient", resourceId);
        auditLog.setDescription("Patient record created");
        auditLog.setIpAddress("192.168.1.1");

        // Test basic properties
        assertThat(auditLog.getUserId()).isEqualTo(userId);
        assertThat(auditLog.getAction()).isEqualTo(com.healthcare.enums.AuditAction.CREATE);
        assertThat(auditLog.getResourceType()).isEqualTo("Patient");
        assertThat(auditLog.getResourceId()).isEqualTo(resourceId);
        assertThat(auditLog.getDescription()).isEqualTo("Patient record created");
        assertThat(auditLog.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(auditLog.getTimestamp()).isNotNull();

        // Test additional setters
        auditLog.setUserAgent("Mozilla/5.0");

        assertThat(auditLog.getUserAgent()).isEqualTo("Mozilla/5.0");
    }
}
