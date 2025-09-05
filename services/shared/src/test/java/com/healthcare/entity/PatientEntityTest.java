package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Patient entity
 */
class PatientEntityTest {

    @Test
    void testPatientEntity() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        String testMedicalHistory = "No significant medical history";
        String testAllergies = "Peanuts";
        String testEmergencyContactName = "John Smith";
        String testEmergencyContactPhone = "+15551234567";

        // Create patient
        Patient patient = new Patient(testUserId, testPatientNumber);
        patient.setMedicalHistory(testMedicalHistory);

        // Test basic properties
        assertThat(patient.getUserId()).isEqualTo(testUserId);
        assertThat(patient.getPatientNumber()).isEqualTo(testPatientNumber);
        assertThat(patient.getMedicalHistory()).isEqualTo(testMedicalHistory);

        // Test additional setters
        patient.setAllergies(testAllergies);
        assertThat(patient.getAllergies()).isEqualTo(testAllergies);

        patient.setEmergencyContactName(testEmergencyContactName);
        patient.setEmergencyContactPhone(testEmergencyContactPhone);
        assertThat(patient.getEmergencyContactName()).isEqualTo(testEmergencyContactName);
        assertThat(patient.getEmergencyContactPhone()).isEqualTo(testEmergencyContactPhone);
    }

    @Test
    void testPatientValidationMethods() {
        // Test data variables
        String testExternalAuthId = "ext-auth-789";
        String testFirstName = "Test";
        String testLastName = "Patient";
        String testEmail = "test@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 5, 15);
        Gender testGender = Gender.FEMALE;
        UserRole testRole = UserRole.PATIENT;
        String testPatientNumber = "PAT-87654321";
        String testEmergencyContactName = "Emergency Contact";
        String testEmergencyContactPhone = "+15551234567";
        String testInsuranceProvider = "Blue Cross";
        String testInsurancePolicyNumber = "BC123456789";
        String testMedicalHistory = "Previous surgery in 2020";
        String testAllergies = "Peanuts, Shellfish";
        String testCurrentMedications = "Lisinopril 10mg daily";

        UUID testUserId = UUID.randomUUID();

        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test validation methods
        assertThat(patient.hasValidPatientNumber()).isTrue();
        assertThat(patient.isReadyForAppointments()).isTrue();

        // Test emergency contact validation
        patient.setEmergencyContactName(testEmergencyContactName);
        patient.setEmergencyContactPhone(testEmergencyContactPhone);
        assertThat(patient.hasCompleteEmergencyContact()).isTrue();

        // Test insurance validation
        patient.setInsuranceProvider(testInsuranceProvider);
        patient.setInsurancePolicyNumber(testInsurancePolicyNumber);
        assertThat(patient.hasInsuranceInfo()).isTrue();

        // Test medical history validation
        patient.setMedicalHistory(testMedicalHistory);
        assertThat(patient.hasMedicalHistory()).isTrue();

        // Test allergy validation
        patient.setAllergies(testAllergies);
        assertThat(patient.hasAllergyInfo()).isTrue();

        // Test medication validation
        patient.setCurrentMedications(testCurrentMedications);
        assertThat(patient.hasCurrentMedications()).isTrue();
    }
}
