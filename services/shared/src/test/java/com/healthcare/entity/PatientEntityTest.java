package com.healthcare.entity;

import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for Patient entity
 */
class PatientEntityTest {
    private String first_name = "John";
    private String last_name = "Doe";

    private Patient createPatient() {
        return new Patient(first_name, last_name);
    }

    @Test
    void testConstructorAndBasicGetters() {
        Patient patient = createPatient();

        assertThat(patient.getFirstName()).isEqualTo(first_name);
        assertThat(patient.getLastName()).isEqualTo(last_name);
        assertThat(patient.getFullName()).isEqualTo(first_name + " " + last_name);
        assertThat(patient.getAuthId()).isNull();
        assertThat(patient.getMrn()).isNull();
        assertThat(patient.getUser()).isNull();
    }

    @Test
    void testConstructorValidation() {
        assertThatThrownBy(() -> new Patient(null, last_name))
                .isInstanceOf(ValidationException.class)
                .hasMessage("First name is required");

        assertThatThrownBy(() -> new Patient(first_name, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Last name is required");

        assertThatThrownBy(() -> new Patient("  ", last_name))
                .isInstanceOf(ValidationException.class)
                .hasMessage("First name is required");

        assertThatThrownBy(() -> new Patient(first_name, "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Last name is required");
    }

    @Test
    void testSettersAndNormalizers() {
        Patient patient = createPatient();
        String phone = "+15551234567";
        String emergencyContact = "Jane Doe";
        String note = "patient notes";

        patient.setPhone(phone);

        patient.setPhone("   ");
        assertThat(patient.getPhone()).isNull();

        patient.setEmergencyContact(emergencyContact);
        assertThat(patient.getEmergencyContact()).isEqualTo(emergencyContact);

        patient.setEmergencyContact("");
        assertThat(patient.getEmergencyContact()).isNull();

        patient.setNotes(note);
        assertThat(patient.getNotes()).isEqualTo(note);
    }

    @Test
    void testPhoneValidation() {
        Patient patient = createPatient();

        assertThatThrownBy(() -> patient.setPhone("0123456789"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone must be a valid international format");

        String tooLongPhone = "+123456789012345678901";
        assertThatThrownBy(() -> patient.setPhone(tooLongPhone))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone cannot exceed 20 characters");
    }

    @Test
    void testIsRegisteredAndLinkAuthAccount() {
        Patient patient = createPatient();
        UUID userId = UUID.randomUUID();

        assertThat(patient.isRegistered()).isFalse();
        patient.linkAuthAccount(userId);
        assertThat(patient.isRegistered()).isTrue();
        assertThat(patient.getAuthId()).isEqualTo(userId);

        assertThatThrownBy(() -> patient.linkAuthAccount(UUID.randomUUID()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Patient is already linked to an account");
    }

    @Test
    void testLinkAuthAccountValidation() {
        Patient patient = createPatient();

        assertThatThrownBy(() -> patient.linkAuthAccount(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User ID cannot be null");
    }

    @Test
    void testMatchesRegistrationCredentials() {
        Patient patient = createPatient();
        String mrn = "MRN-000123";

        try {
            java.lang.reflect.Field mrnField = Patient.class.getDeclaredField("mrn");
            mrnField.setAccessible(true);
            mrnField.set(patient, mrn);
        } catch (Exception e) {
            fail("Failed to set mrn field via reflection: " + e.getMessage());
        }

        assertThat(patient.matchesRegistrationCredentials(mrn, first_name, last_name)).isTrue();
        assertThat(patient.matchesRegistrationCredentials("MRN-999999", first_name, last_name)).isFalse();
        assertThat(patient.matchesRegistrationCredentials(null, first_name, last_name)).isFalse();
        assertThat(patient.matchesRegistrationCredentials(mrn, null, last_name)).isFalse();
        assertThat(patient.matchesRegistrationCredentials(mrn, first_name, null)).isFalse();
    }

    @Test
    void testGetUser() {
        Patient patient = createPatient();

        User user = new User(
                "john_doe",
                "john.doe@example.com",
                "$2a$10$abcdefghijklmnopqrstuv",
                com.healthcare.enums.UserRole.PATIENT
        );
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
}
