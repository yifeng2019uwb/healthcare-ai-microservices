package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

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
        String testInsurancePolicyNumber = "BC1234567890";
        String testMedicalHistory = "Previous surgery in 2020";
        String testAllergies = "Peanuts, Shellfish";
        String testCurrentMedications = "Lisinopril 10mg daily";

        UUID testUserId = UUID.randomUUID();

        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test basic field access
        assertThat(patient.getUserId()).isEqualTo(testUserId);
        assertThat(patient.getPatientNumber()).isEqualTo(testPatientNumber);

        // Test emergency contact fields
        patient.setEmergencyContactName(testEmergencyContactName);
        patient.setEmergencyContactPhone(testEmergencyContactPhone);
        assertThat(patient.getEmergencyContactName()).isEqualTo(testEmergencyContactName);
        assertThat(patient.getEmergencyContactPhone()).isEqualTo(testEmergencyContactPhone);
        assertThat(patient.hasCompleteEmergencyContact()).isTrue();

        // Test insurance fields
        patient.setInsuranceProvider(testInsuranceProvider);
        patient.setInsurancePolicyNumber(testInsurancePolicyNumber);
        assertThat(patient.getInsuranceProvider()).isEqualTo(testInsuranceProvider);
        assertThat(patient.getInsurancePolicyNumber()).isEqualTo(testInsurancePolicyNumber);
        assertThat(patient.hasCompleteInsuranceInfo()).isTrue();

        // Test medical history field
        patient.setMedicalHistory(testMedicalHistory);
        assertThat(patient.getMedicalHistory()).isEqualTo(testMedicalHistory);

        // Test allergies field
        patient.setAllergies(testAllergies);
        assertThat(patient.getAllergies()).isEqualTo(testAllergies);

        // Test medications field
        patient.setCurrentMedications(testCurrentMedications);
        assertThat(patient.getCurrentMedications()).isEqualTo(testCurrentMedications);
    }

    @Test
    void testMedicalHistoryField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testMedicalHistory = "Previous surgery in 2020";
        patient.setMedicalHistory(testMedicalHistory);
        assertThat(patient.getMedicalHistory()).isEqualTo(testMedicalHistory);

        // Test setter with null value (allowed)
        patient.setMedicalHistory(null);
        assertThat(patient.getMedicalHistory()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setMedicalHistory("");
        assertThat(patient.getMedicalHistory()).isNull();

        // Test setter with whitespace value (normalized to null)
        patient.setMedicalHistory("   ");
        assertThat(patient.getMedicalHistory()).isNull();
    }

    @Test
    void testAllergiesField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testAllergies = "Peanuts, Shellfish";
        patient.setAllergies(testAllergies);
        assertThat(patient.getAllergies()).isEqualTo(testAllergies);

        // Test setter with null value (allowed)
        patient.setAllergies(null);
        assertThat(patient.getAllergies()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setAllergies("");
        assertThat(patient.getAllergies()).isNull();

        // Test setter with whitespace value (normalized to null)
        patient.setAllergies("   ");
        assertThat(patient.getAllergies()).isNull();
    }

    @Test
    void testCurrentMedicationsField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testMedications = "Lisinopril 10mg daily";
        patient.setCurrentMedications(testMedications);
        assertThat(patient.getCurrentMedications()).isEqualTo(testMedications);

        // Test setter with null value (allowed)
        patient.setCurrentMedications(null);
        assertThat(patient.getCurrentMedications()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setCurrentMedications("");
        assertThat(patient.getCurrentMedications()).isNull();

        // Test setter validation - too long
        String longMedications = "A".repeat(2001);
        assertThatThrownBy(() -> patient.setCurrentMedications(longMedications))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Current medications cannot exceed 2000 characters");
    }

    @Test
    void testInsuranceProviderField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testProvider = "Blue Cross Blue Shield";
        patient.setInsuranceProvider(testProvider);
        assertThat(patient.getInsuranceProvider()).isEqualTo(testProvider);

        // Test setter with null value (allowed)
        patient.setInsuranceProvider(null);
        assertThat(patient.getInsuranceProvider()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setInsuranceProvider("");
        assertThat(patient.getInsuranceProvider()).isNull();

        // Test setter validation - too long
        String longProvider = "A".repeat(101);
        assertThatThrownBy(() -> patient.setInsuranceProvider(longProvider))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Insurance provider cannot exceed 100 characters");
    }

    @Test
    void testInsurancePolicyNumberField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testPolicyNumber = "BC1234567890";
        patient.setInsurancePolicyNumber(testPolicyNumber);
        assertThat(patient.getInsurancePolicyNumber()).isEqualTo(testPolicyNumber);

        // Test setter with null value (allowed)
        patient.setInsurancePolicyNumber(null);
        assertThat(patient.getInsurancePolicyNumber()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setInsurancePolicyNumber("");
        assertThat(patient.getInsurancePolicyNumber()).isNull();

        // Test setter validation - too long
        String longPolicyNumber = "A".repeat(51);
        assertThatThrownBy(() -> patient.setInsurancePolicyNumber(longPolicyNumber))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Insurance policy number cannot exceed 50 characters");

        // Test setter validation - invalid format (too short)
        assertThatThrownBy(() -> patient.setInsurancePolicyNumber("12345"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Insurance policy number must be 6-25 alphanumeric characters");
    }

    @Test
    void testEmergencyContactNameField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testContactName = "John Smith";
        patient.setEmergencyContactName(testContactName);
        assertThat(patient.getEmergencyContactName()).isEqualTo(testContactName);

        // Test setter with null value (allowed)
        patient.setEmergencyContactName(null);
        assertThat(patient.getEmergencyContactName()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setEmergencyContactName("");
        assertThat(patient.getEmergencyContactName()).isNull();

        // Test setter validation - too long
        String longContactName = "A".repeat(101);
        assertThatThrownBy(() -> patient.setEmergencyContactName(longContactName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Emergency contact name cannot exceed 100 characters");
    }

    @Test
    void testEmergencyContactPhoneField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testContactPhone = "+15551234567";
        patient.setEmergencyContactPhone(testContactPhone);
        assertThat(patient.getEmergencyContactPhone()).isEqualTo(testContactPhone);

        // Test setter with null value (allowed)
        patient.setEmergencyContactPhone(null);
        assertThat(patient.getEmergencyContactPhone()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setEmergencyContactPhone("");
        assertThat(patient.getEmergencyContactPhone()).isNull();

        // Test setter validation - too long
        String longContactPhone = "+123456789012345678901";
        assertThatThrownBy(() -> patient.setEmergencyContactPhone(longContactPhone))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Emergency contact phone cannot exceed 20 characters");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> patient.setEmergencyContactPhone("0123456789"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Emergency contact phone must be a valid international format");
    }

    @Test
    void testPrimaryCarePhysicianField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with valid value
        String testPhysician = "Dr. Jane Smith";
        patient.setPrimaryCarePhysician(testPhysician);
        assertThat(patient.getPrimaryCarePhysician()).isEqualTo(testPhysician);

        // Test setter with null value (allowed)
        patient.setPrimaryCarePhysician(null);
        assertThat(patient.getPrimaryCarePhysician()).isNull();

        // Test setter with empty value (normalized to null)
        patient.setPrimaryCarePhysician("");
        assertThat(patient.getPrimaryCarePhysician()).isNull();

        // Test setter validation - too long
        String longPhysician = "A".repeat(101);
        assertThatThrownBy(() -> patient.setPrimaryCarePhysician(longPhysician))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Primary care physician cannot exceed 100 characters");
    }

    @Test
    void testCustomDataField() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test setter with null value (allowed)
        patient.setCustomData(null);
        assertThat(patient.getCustomData()).isNull();

        // Note: JsonNode creation and validation should be handled at service layer
        // Entity only accepts pre-validated JsonNode objects
    }

    @Test
    void testHelperMethodsWithNormalizedNulls() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test with null values - should be false
        assertThat(patient.hasCompleteEmergencyContact()).isFalse();
        assertThat(patient.hasCompleteInsuranceInfo()).isFalse();

        // Test with empty values (normalized to null) - should be false
        patient.setEmergencyContactName("");
        patient.setEmergencyContactPhone("");
        patient.setInsuranceProvider("");
        patient.setInsurancePolicyNumber("");
        assertThat(patient.hasCompleteEmergencyContact()).isFalse();
        assertThat(patient.hasCompleteInsuranceInfo()).isFalse();

        // Test with valid values - should be true
        patient.setEmergencyContactName("John Smith");
        patient.setEmergencyContactPhone("+15551234567");
        patient.setInsuranceProvider("Blue Cross");
        patient.setInsurancePolicyNumber("BC1234567890");
        assertThat(patient.hasCompleteEmergencyContact()).isTrue();
        assertThat(patient.hasCompleteInsuranceInfo()).isTrue();
    }

    @Test
    void testHelperMethodsPartialData() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Test emergency contact with only name - should be false
        patient.setEmergencyContactName("John Smith");
        patient.setEmergencyContactPhone(null);
        assertThat(patient.hasCompleteEmergencyContact()).isFalse();

        // Test emergency contact with only phone - should be false
        patient.setEmergencyContactName(null);
        patient.setEmergencyContactPhone("+15551234567");
        assertThat(patient.hasCompleteEmergencyContact()).isFalse();

        // Test insurance with only provider - should be false
        patient.setInsuranceProvider("Blue Cross");
        patient.setInsurancePolicyNumber(null);
        assertThat(patient.hasCompleteInsuranceInfo()).isFalse();

        // Test insurance with only policy number - should be false
        patient.setInsuranceProvider(null);
        patient.setInsurancePolicyNumber("BC1234567890");
        assertThat(patient.hasCompleteInsuranceInfo()).isFalse();
    }

    @Test
    void testGetUser() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Initially should be null
        assertThat(patient.getUser()).isNull();

        // Create a user and set it (using reflection since there's no setter)
        User user = new User();
        try {
            java.lang.reflect.Field userField = Patient.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(patient, user);
        } catch (Exception e) {
            fail("Failed to set user field via reflection: " + e.getMessage());
        }

        // Now should return the user
        assertThat(patient.getUser()).isEqualTo(user);
    }

    @Test
    void testGetAppointments() {
        UUID testUserId = UUID.randomUUID();
        String testPatientNumber = "PAT-12345678";
        Patient patient = new Patient(testUserId, testPatientNumber);

        // Initially should return empty list (not null)
        assertThat(patient.getAppointments()).isNotNull();
        assertThat(patient.getAppointments()).isEmpty();

        // Create an appointment and add it to the list
        Appointment appointment = new Appointment();
        patient.getAppointments().add(appointment);

        // Now should contain the appointment
        assertThat(patient.getAppointments()).hasSize(1);
        assertThat(patient.getAppointments()).contains(appointment);
    }
}
