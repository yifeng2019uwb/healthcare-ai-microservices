package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private String mrn = "MRN-000001";

    private Patient createPatient() {
        return new Patient(mrn, first_name, last_name);
    }

    @Test
    void testConstructorAndBasicGetters() {
        Patient patient = createPatient();

        assertThat(patient.getFirstName()).isEqualTo(first_name);
        assertThat(patient.getLastName()).isEqualTo(last_name);
        assertThat(patient.getFullName()).isEqualTo(first_name + " " + last_name);
        assertThat(patient.getMrn()).isEqualTo(mrn);
        assertThat(patient.getAuthId()).isNull();
        assertThat(patient.getUser()).isNull();
    }

    @Test
    void testConstructorValidation() {
        assertThatThrownBy(() -> new Patient(null, first_name, last_name))
                .isInstanceOf(ValidationException.class)
                .hasMessage("MRN is required");

        assertThatThrownBy(() -> new Patient(mrn, null, last_name))
                .isInstanceOf(ValidationException.class)
                .hasMessage("First name is required");

        assertThatThrownBy(() -> new Patient(mrn, first_name, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Last name is required");

        assertThatThrownBy(() -> new Patient(mrn, "  ", last_name))
                .isInstanceOf(ValidationException.class)
                .hasMessage("First name is required");

        assertThatThrownBy(() -> new Patient(mrn, first_name, "   "))
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

        assertThat(patient.getUser()).isEqualTo(user);
    }

    @Test
    void getDemographicFields_nullByDefault() {
        Patient patient = createPatient();
        assertThat(patient.getBirthdate()).isNull();
        assertThat(patient.getDeathdate()).isNull();
        assertThat(patient.getSsn()).isNull();
        assertThat(patient.getDrivers()).isNull();
        assertThat(patient.getPassport()).isNull();
        assertThat(patient.getPrefix()).isNull();
        assertThat(patient.getMiddleName()).isNull();
        assertThat(patient.getSuffix()).isNull();
        assertThat(patient.getMaiden()).isNull();
        assertThat(patient.getMarital()).isNull();
        assertThat(patient.getRace()).isNull();
        assertThat(patient.getEthnicity()).isNull();
        assertThat(patient.getGender()).isNull();
        assertThat(patient.getBirthplace()).isNull();
        assertThat(patient.getAddress()).isNull();
        assertThat(patient.getCity()).isNull();
        assertThat(patient.getState()).isNull();
        assertThat(patient.getCounty()).isNull();
        assertThat(patient.getFips()).isNull();
        assertThat(patient.getZip()).isNull();
        assertThat(patient.getLat()).isNull();
        assertThat(patient.getLon()).isNull();
        assertThat(patient.getHealthcareExpenses()).isNull();
        assertThat(patient.getHealthcareCoverage()).isNull();
        assertThat(patient.getIncome()).isNull();
        assertThat(patient.getBloodType()).isNull();
    }

    @Test
    void setDemographicFields() {
        Patient patient = createPatient();
        LocalDate birthdate = LocalDate.of(1990, 5, 15);
        LocalDate deathdate = LocalDate.of(2050, 1, 1);
        BigDecimal lat = new BigDecimal("47.608013");
        BigDecimal lon = new BigDecimal("-122.335167");
        BigDecimal expenses = new BigDecimal("1500.00");
        BigDecimal coverage = new BigDecimal("1200.00");

        patient.setBirthdate(birthdate);
        patient.setDeathdate(deathdate);
        patient.setSsn("999-99-9999");
        patient.setDrivers("S99999999");
        patient.setPassport("X12345678");
        patient.setPrefix("Mr.");
        patient.setSuffix("Jr.");
        patient.setMaiden("Smith");
        patient.setMarital("M");
        patient.setRace("white");
        patient.setEthnicity("nonhispanic");
        patient.setGender(Gender.M);
        patient.setBirthplace("Boston MA US");
        patient.setAddress("123 Main St");
        patient.setCity("Seattle");
        patient.setState("WA");
        patient.setCounty("King");
        patient.setFips("53033");
        patient.setZip("98101");
        patient.setLat(lat);
        patient.setLon(lon);
        patient.setHealthcareExpenses(expenses);
        patient.setHealthcareCoverage(coverage);
        patient.setIncome(75000);
        patient.setBloodType("O+");

        assertThat(patient.getBirthdate()).isEqualTo(birthdate);
        assertThat(patient.getDeathdate()).isEqualTo(deathdate);
        assertThat(patient.getSsn()).isEqualTo("999-99-9999");
        assertThat(patient.getDrivers()).isEqualTo("S99999999");
        assertThat(patient.getPassport()).isEqualTo("X12345678");
        assertThat(patient.getPrefix()).isEqualTo("Mr.");
        assertThat(patient.getSuffix()).isEqualTo("Jr.");
        assertThat(patient.getMaiden()).isEqualTo("Smith");
        assertThat(patient.getMarital()).isEqualTo("M");
        assertThat(patient.getRace()).isEqualTo("white");
        assertThat(patient.getEthnicity()).isEqualTo("nonhispanic");
        assertThat(patient.getGender()).isEqualTo(Gender.M);
        assertThat(patient.getBirthplace()).isEqualTo("Boston MA US");
        assertThat(patient.getAddress()).isEqualTo("123 Main St");
        assertThat(patient.getCity()).isEqualTo("Seattle");
        assertThat(patient.getState()).isEqualTo("WA");
        assertThat(patient.getCounty()).isEqualTo("King");
        assertThat(patient.getFips()).isEqualTo("53033");
        assertThat(patient.getZip()).isEqualTo("98101");
        assertThat(patient.getLat()).isEqualByComparingTo(lat);
        assertThat(patient.getLon()).isEqualByComparingTo(lon);
        assertThat(patient.getHealthcareExpenses()).isEqualByComparingTo(expenses);
        assertThat(patient.getHealthcareCoverage()).isEqualByComparingTo(coverage);
        assertThat(patient.getIncome()).isEqualTo(75000);
        assertThat(patient.getBloodType()).isEqualTo("O+");
    }

    @Test
    void setFirstName_normalizesBlankToNull() {
        Patient patient = createPatient();
        patient.setFirstName("Jane");
        assertThat(patient.getFirstName()).isEqualTo("Jane");
        patient.setFirstName("   ");
        assertThat(patient.getFirstName()).isNull();
    }

    @Test
    void setMiddleName_normalizesBlankToNull() {
        Patient patient = createPatient();
        patient.setMiddleName("Marie");
        assertThat(patient.getMiddleName()).isEqualTo("Marie");
        patient.setMiddleName("");
        assertThat(patient.getMiddleName()).isNull();
    }

    @Test
    void setLastName_normalizesBlankToNull() {
        Patient patient = createPatient();
        patient.setLastName("Smith");
        assertThat(patient.getLastName()).isEqualTo("Smith");
        patient.setLastName("   ");
        assertThat(patient.getLastName()).isNull();
    }
}