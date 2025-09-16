package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for User entity
 * Following strategy: one test per field/attribute/method
 */
class UserEntityTest {

    // Test data variables
    private static final String testExternalAuthId = "auth_123456";
    private static final String testFirstName = "John";
    private static final String testLastName = "Doe";
    private static final String testEmail = "john.doe@example.com";
    private static final String testPhone = "+1234567890";
    private static final LocalDate testDateOfBirth = LocalDate.now().minusYears(25);
    private static final Gender testGender = Gender.MALE;
    private static final UserRole testRole = UserRole.PATIENT;

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void testUserConstructor() {
        User user = new User(testExternalAuthId, testEmail, testRole);

        assertThat(user.getExternalAuthId()).isEqualTo(testExternalAuthId);
        assertThat(user.getEmail()).isEqualTo(testEmail);
        assertThat(user.getRole()).isEqualTo(testRole);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE); // Default value

        // Set optional fields
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        assertThat(user.getFirstName()).isEqualTo(testFirstName);
        assertThat(user.getLastName()).isEqualTo(testLastName);
        assertThat(user.getPhone()).isEqualTo(testPhone);
        assertThat(user.getDateOfBirth()).isEqualTo(testDateOfBirth);
        assertThat(user.getGender()).isEqualTo(testGender);
    }

    // ==================== FIELD TESTS (with getter/setter) ====================

    @Test
    void testFirstNameField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access)
        assertThat(user.getFirstName()).isEqualTo(testFirstName);

        // Test setter with valid value
        String newFirstName = "Jane";
        user.setFirstName(newFirstName);
        assertThat(user.getFirstName()).isEqualTo(newFirstName);

        // Test setter validation - null value
        assertThatThrownBy(() -> user.setFirstName(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name cannot be null or empty");

        // Test setter validation - empty value
        assertThatThrownBy(() -> user.setFirstName(""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name cannot be null or empty");

        // Test setter validation - too long
        String longName = "A".repeat(101);
        assertThatThrownBy(() -> user.setFirstName(longName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name cannot exceed 100 characters");

        // Test setter validation - invalid format (numbers)
        assertThatThrownBy(() -> user.setFirstName("John123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("First name format is invalid");

        // Test setter with valid international characters
        String internationalFirstName = "José";
        user.setFirstName(internationalFirstName);
        assertThat(user.getFirstName()).isEqualTo(internationalFirstName);

        // Test setter with valid compound name
        String compoundFirstName = "Mary Jane";
        user.setFirstName(compoundFirstName);
        assertThat(user.getFirstName()).isEqualTo(compoundFirstName);
    }

    @Test
    void testLastNameField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access)
        assertThat(user.getLastName()).isEqualTo(testLastName);

        // Test setter with valid value
        String newLastName = "Smith";
        user.setLastName(newLastName);
        assertThat(user.getLastName()).isEqualTo(newLastName);

        // Test setter validation - null value
        assertThatThrownBy(() -> user.setLastName(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name cannot be null or empty");

        // Test setter validation - empty value
        assertThatThrownBy(() -> user.setLastName(""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name cannot be null or empty");

        // Test setter validation - too long
        String longLastName = "A".repeat(101);
        assertThatThrownBy(() -> user.setLastName(longLastName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name cannot exceed 100 characters");

        // Test setter validation - invalid format (numbers)
        assertThatThrownBy(() -> user.setLastName("Smith123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Last name format is invalid");

        // Test setter with valid international characters
        String internationalLastName = "García";
        user.setLastName(internationalLastName);
        assertThat(user.getLastName()).isEqualTo(internationalLastName);

        // Test setter with valid hyphenated name
        String hyphenatedLastName = "Smith-Jones";
        user.setLastName(hyphenatedLastName);
        assertThat(user.getLastName()).isEqualTo(hyphenatedLastName);
    }

    @Test
    void testEmailField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access)
        assertThat(user.getEmail()).isEqualTo(testEmail);

        // Test setter with valid value
        String newEmail = "jane.smith@example.com";
        user.setEmail(newEmail);
        assertThat(user.getEmail()).isEqualTo(newEmail);

        // Test setter validation - null value
        assertThatThrownBy(() -> user.setEmail(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email cannot be null or empty");

        // Test setter validation - empty value
        assertThatThrownBy(() -> user.setEmail(""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email cannot be null or empty");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setEmail("invalid-email"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email format is invalid");

        // Test setter validation - too long
        String longEmail = "a".repeat(250) + "@example.com";
        assertThatThrownBy(() -> user.setEmail(longEmail))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email cannot exceed 255 characters");
    }

    @Test
    void testPhoneField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access)
        assertThat(user.getPhone()).isEqualTo(testPhone);

        // Test setter with valid value
        String newPhone = "+9876543210";
        user.setPhone(newPhone);
        assertThat(user.getPhone()).isEqualTo(newPhone);

        // Test setter validation - null value
        assertThatThrownBy(() -> user.setPhone(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone cannot be null or empty");

        // Test setter validation - empty value
        assertThatThrownBy(() -> user.setPhone(""))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone cannot be null or empty");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setPhone("0123456789"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone number format is invalid");

        // Test setter validation - too long
        String longPhone = "+123456789012345678901";
        assertThatThrownBy(() -> user.setPhone(longPhone))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone cannot exceed 20 characters");
    }

    @Test
    void testDateOfBirthField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access)
        assertThat(user.getDateOfBirth()).isEqualTo(testDateOfBirth);

        // Test setter with valid value
        LocalDate newDate = LocalDate.of(1985, 5, 15);
        user.setDateOfBirth(newDate);
        assertThat(user.getDateOfBirth()).isEqualTo(newDate);

        // Test setter validation - null value not allowed
        assertThatThrownBy(() -> user.setDateOfBirth(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date of birth cannot be null");

        // Test setter validation - future date
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> user.setDateOfBirth(futureDate))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date of birth cannot be in the future");
    }

    @Test
    void testGenderField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access)
        assertThat(user.getGender()).isEqualTo(testGender);

        // Test setter with valid value
        user.setGender(Gender.FEMALE);
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);

        // Test setter validation - null value not allowed
        assertThatThrownBy(() -> user.setGender(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Gender cannot be null");
    }

    @Test
    void testStatusField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test getter (field access) - default value
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);

        // Test setter with valid value
        user.setStatus(UserStatus.INACTIVE);
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);

        // Test setter validation - null value
        assertThatThrownBy(() -> user.setStatus(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Status cannot be null");
    }

    // ==================== ADDRESS FIELD TESTS ====================

    @Test
    void testStreetAddressField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test setter with valid value
        String testStreetAddress = "123 Main St";
        user.setStreetAddress(testStreetAddress);
        assertThat(user.getStreetAddress()).isEqualTo(testStreetAddress);

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setStreetAddress("Invalid@Address!"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Street address format is invalid");

        // Test setter with null value (allowed)
        user.setStreetAddress(null);
        assertThat(user.getStreetAddress()).isNull();
    }

    @Test
    void testCityField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test setter with valid value
        String testCity = "New York";
        user.setCity(testCity);
        assertThat(user.getCity()).isEqualTo(testCity);

        // Test setter validation - too long
        String longCity = "A".repeat(101);
        assertThatThrownBy(() -> user.setCity(longCity))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("City cannot exceed 100 characters");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setCity("New York123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("City name format is invalid");

        // Test setter with null value (allowed)
        user.setCity(null);
        assertThat(user.getCity()).isNull();
    }

    @Test
    void testStateField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test setter with valid value
        String testState = "NY";
        user.setState(testState);
        assertThat(user.getState()).isEqualTo(testState);

        // Test setter validation - too long
        String longState = "A".repeat(51);
        assertThatThrownBy(() -> user.setState(longState))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("State cannot exceed 50 characters");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setState("NY123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("State name format is invalid");

        // Test setter with null value (allowed)
        user.setState(null);
        assertThat(user.getState()).isNull();
    }

    @Test
    void testPostalCodeField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test setter with valid value
        String testPostalCode = "10001";
        user.setPostalCode(testPostalCode);
        assertThat(user.getPostalCode()).isEqualTo(testPostalCode);

        // Test setter with null value (allowed)
        user.setPostalCode(null);
        assertThat(user.getPostalCode()).isNull();

        // Test setter with empty value (normalized to null)
        user.setPostalCode("");
        assertThat(user.getPostalCode()).isNull();

        // Test setter validation - too long
        String longPostalCode = "A".repeat(21);
        assertThatThrownBy(() -> user.setPostalCode(longPostalCode))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Postal code cannot exceed 20 characters");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setPostalCode("12345@#$"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Postal code format is invalid");
    }

    @Test
    void testCountryField() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test setter with valid value
        String testCountry = "USA";
        user.setCountry(testCountry);
        assertThat(user.getCountry()).isEqualTo(testCountry);

        // Test setter validation - too long
        String longCountry = "A".repeat(101);
        assertThatThrownBy(() -> user.setCountry(longCountry))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Country cannot exceed 100 characters");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> user.setCountry("USA123"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Country name format is invalid");

        // Test setter with null value (allowed)
        user.setCountry(null);
        assertThat(user.getCountry()).isNull();
    }

    @Test
    void testCustomDataField() throws IOException {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test setter with valid value
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testCustomData = mapper.readTree("{\"preferences\": \"email\"}");
        user.setCustomData(testCustomData);
        assertThat(user.getCustomData()).isEqualTo(testCustomData);

        // Test setter with null value (allowed)
        user.setCustomData(null);
        assertThat(user.getCustomData()).isNull();
    }

    // ==================== METHOD TESTS ====================

    @Test
    void testIsAdultMethod() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test with adult date of birth
        assertThat(user.isAdult()).isTrue();

        // Test with minor date of birth
        user.setDateOfBirth(LocalDate.now().minusYears(17));
        assertThat(user.isAdult()).isFalse();

    }

    @Test
    void testHasCompleteAddressMethod() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test with no address - should be false
        assertThat(user.hasCompleteAddress()).isFalse();

        // Test with partial address - should be false
        String testStreetAddress = "123 Main St";
        user.setStreetAddress(testStreetAddress);
        assertThat(user.hasCompleteAddress()).isFalse();

        // Test with complete address - should be true
        String testCity = "City";
        String testState = "State";
        String testPostalCode = "12345";
        String testCountry = "Country";
        user.setCity(testCity);
        user.setState(testState);
        user.setPostalCode(testPostalCode);
        user.setCountry(testCountry);

        // Test getters for address fields
        assertThat(user.getStreetAddress()).isEqualTo(testStreetAddress);
        assertThat(user.getCity()).isEqualTo(testCity);
        assertThat(user.getState()).isEqualTo(testState);
        assertThat(user.getPostalCode()).isEqualTo(testPostalCode);
        assertThat(user.getCountry()).isEqualTo(testCountry);

        assertThat(user.hasCompleteAddress()).isTrue();
    }

    @Test
    void testHasCompleteAddressWithWhitespace() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test with whitespace-only values - should be false (trimmed to empty)
        user.setStreetAddress("   ");
        user.setCity("  \t  ");
        user.setState("   ");
        user.setPostalCode("  ");
        user.setCountry("   ");
        assertThat(user.hasCompleteAddress()).isFalse();

        // Test with valid values that have leading/trailing whitespace - should be true
        user.setStreetAddress("  123 Main St  ");
        assertThat(user.hasCompleteAddress()).isFalse();
        user.setCity("  New York  ");
        assertThat(user.hasCompleteAddress()).isFalse();
        user.setState("  NY  ");
        assertThat(user.hasCompleteAddress()).isFalse();
        user.setPostalCode("  10001  ");
        assertThat(user.hasCompleteAddress()).isFalse();
        user.setCountry("  USA  ");
        assertThat(user.hasCompleteAddress()).isTrue();
    }

    @Test
    void testHasCompleteAddressWithNullValues() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test with null values - should be false
        assertThat(user.hasCompleteAddress()).isFalse();

        // Test with mixed null and empty values - should be false
        user.setStreetAddress("123 Main St");
        user.setCity(null);
        user.setState("");  // Will be normalized to null
        user.setPostalCode("   ");  // Will be normalized to null
        user.setCountry("USA");
        assertThat(user.hasCompleteAddress()).isFalse();
    }

    @Test
    void testIsActiveMethod() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test with ACTIVE status
        user.setStatus(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();

        // Test with INACTIVE status
        user.setStatus(UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();

        // Test with SUSPENDED status
        user.setStatus(UserStatus.SUSPENDED);
        assertThat(user.isActive()).isFalse();
    }


    @Test
    void testGetFullNameMethod() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Test full name generation
        String expectedFullName = testFirstName + " " + testLastName;
        assertThat(user.getFullName()).isEqualTo(expectedFullName);

        // Test with updated names
        String newFirstName = "Jane";
        String newLastName = "Smith";
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);
        assertThat(user.getFullName()).isEqualTo(newFirstName + " " + newLastName);
    }

    @Test
    void testGetPatient() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Initially should be null
        assertThat(user.getPatient()).isNull();

        // Create a patient and set it (using reflection since there's no setter)
        Patient patient = new Patient(java.util.UUID.randomUUID(), "PAT-123456");
        try {
            java.lang.reflect.Field patientField = User.class.getDeclaredField("patient");
            patientField.setAccessible(true);
            patientField.set(user, patient);
        } catch (Exception e) {
            fail("Failed to set patient field via reflection: " + e.getMessage());
        }

        // Now should return the patient
        assertThat(user.getPatient()).isEqualTo(patient);
    }

    @Test
    void testGetProvider() {
        User user = new User(testExternalAuthId, testEmail, testRole);
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setPhone(testPhone);
        user.setDateOfBirth(testDateOfBirth);
        user.setGender(testGender);

        // Initially should be null
        assertThat(user.getProvider()).isNull();

        // Create a provider and set it (using reflection since there's no setter)
        Provider provider = new Provider(java.util.UUID.randomUUID(), "1234567890");
        try {
            java.lang.reflect.Field providerField = User.class.getDeclaredField("provider");
            providerField.setAccessible(true);
            providerField.set(user, provider);
        } catch (Exception e) {
            fail("Failed to set provider field via reflection: " + e.getMessage());
        }

        // Now should return the provider
        assertThat(user.getProvider()).isEqualTo(provider);
    }
}