package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import com.healthcare.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for User entity
 */
class UserEntityTest {

    @Test
    void testUserEntity() {
        // Test data variables
        String testExternalAuthId = "ext-auth-123";
        String testFirstName = "John";
        String testLastName = "Doe";
        String testEmail = "john.doe@gmail.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 5, 15);
        Gender testGender = Gender.MALE;
        UserRole testRole = UserRole.PATIENT;

        // Create a user using the correct constructor
        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        // Test basic properties
        assertThat(user.getFirstName()).isEqualTo(testFirstName);
        assertThat(user.getLastName()).isEqualTo(testLastName);
        assertThat(user.getEmail()).isEqualTo(testEmail);
        assertThat(user.getRole()).isEqualTo(testRole);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();

        // Test enum values for MALE gender and PATIENT role
        assertThat(user.getGender()).isEqualTo(Gender.MALE);
        assertThat(user.getGender().getCode()).isEqualTo(Gender.MALE.getCode());
        assertThat(user.getGender().getDescription()).isEqualTo(Gender.MALE.getDescription());
        assertThat(user.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(user.getRole().getCode()).isEqualTo(UserRole.PATIENT.getCode());
        assertThat(user.getRole().getDescription()).isEqualTo(UserRole.PATIENT.getDescription());

        // Test setters
        user.setPhone(testPhone);
        assertThat(user.getPhone()).isEqualTo(testPhone);

        user.setDateOfBirth(testDateOfBirth);
        assertThat(user.getDateOfBirth()).isEqualTo(testDateOfBirth);

        user.setGender(testGender);
        assertThat(user.getGender()).isEqualTo(testGender);

        // Address test data
        String testStreetAddress = "123 Main St";
        String testCity = "City";
        String testState = "State";
        String testPostalCode = "12345";
        String testCountry = "USA";

        user.setStreetAddress(testStreetAddress);
        user.setCity(testCity);
        user.setState(testState);
        user.setPostalCode(testPostalCode);
        user.setCountry(testCountry);
        assertThat(user.getStreetAddress()).isEqualTo(testStreetAddress);
        assertThat(user.getCity()).isEqualTo(testCity);
        assertThat(user.getState()).isEqualTo(testState);
        assertThat(user.getPostalCode()).isEqualTo(testPostalCode);
        assertThat(user.getCountry()).isEqualTo(testCountry);

        user.setStatus(UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void testUserValidationMethods() {
        // Test data variables
        String testExternalAuthId = "ext-auth-456";
        String testFirstName = "Jane";
        String testLastName = "Smith";
        String testEmail = "jane.smith@gmail.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 5, 15);
        Gender testGender = Gender.OTHER;
        UserRole testRole = UserRole.PROVIDER;

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        // Test validation methods
        assertThat(user.isAdult()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(user.canBeUpdated()).isTrue();
        assertThat(user.isDeleted()).isFalse();

        // Address test data
        String testStreetAddress = "123 Main St";
        String testCity = "City";
        String testState = "State";
        String testPostalCode = "12345";
        String testCountry = "USA";

        // Test address validation
        user.setStreetAddress(testStreetAddress);
        user.setCity(testCity);
        user.setState(testState);
        user.setPostalCode(testPostalCode);
        user.setCountry(testCountry);
        assertThat(user.hasCompleteAddress()).isTrue();

        // Test phone validation
        assertThat(user.hasValidPhoneNumber()).isTrue();

        // Test email validation
        assertThat(user.hasValidHealthcareEmail()).isTrue();

        // Test enum values for OTHER gender and PROVIDER role
        assertThat(user.getGender()).isEqualTo(Gender.OTHER);
        assertThat(user.getGender().getCode()).isEqualTo(Gender.OTHER.getCode());
        assertThat(user.getGender().getDescription()).isEqualTo(Gender.OTHER.getDescription());
        assertThat(user.getRole()).isEqualTo(UserRole.PROVIDER);
        assertThat(user.getRole().getCode()).isEqualTo(UserRole.PROVIDER.getCode());
        assertThat(user.getRole().getDescription()).isEqualTo(UserRole.PROVIDER.getDescription());
    }

    @Test
    void testUserEdgeCases() {
        // Test data variables
        String testExternalAuthId = "ext-auth-edge";
        String testFirstName = "Edge";
        String testLastName = "Case";
        String testEmail = "edge.case@gmail.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 5, 15);
        Gender testGender = Gender.FEMALE;
        UserRole testRole = UserRole.PATIENT;

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        // Test edge cases for validation methods
        // Test invalid email domains
        user.setEmail("invalid@example.com");
        assertThat(user.hasValidHealthcareEmail()).isFalse();

        // Test valid email domains
        user.setEmail("valid@gmail.com");
        assertThat(user.hasValidHealthcareEmail()).isTrue();

        user.setEmail("valid@outlook.com");
        assertThat(user.hasValidHealthcareEmail()).isTrue();

        user.setEmail("valid@healthcare.gov");
        assertThat(user.hasValidHealthcareEmail()).isTrue();

        user.setEmail("valid@hospital.org");
        assertThat(user.hasValidHealthcareEmail()).isTrue();

        // Test invalid phone numbers
        user.setPhone("invalid-phone");
        assertThat(user.hasValidPhoneNumber()).isFalse();

        user.setPhone("123");
        assertThat(user.hasValidPhoneNumber()).isFalse();

        // Test valid phone numbers
        user.setPhone("+1234567890");
        assertThat(user.hasValidPhoneNumber()).isTrue();

        user.setPhone("(123) 456-7890");
        assertThat(user.hasValidPhoneNumber()).isTrue();

        user.setPhone("123-456-7890");
        assertThat(user.hasValidPhoneNumber()).isTrue();

        // Test incomplete address
        user.setStreetAddress("123 Main St");
        user.setCity("City");
        // Missing state, postal code, country
        assertThat(user.hasCompleteAddress()).isFalse();

        // Complete address
        user.setState("State");
        user.setPostalCode("12345");
        user.setCountry("USA");
        assertThat(user.hasCompleteAddress()).isTrue();

        // Test age validation
        user.setDateOfBirth(LocalDate.now().minusYears(17)); // Under 18
        assertThat(user.isAdult()).isFalse();

        user.setDateOfBirth(LocalDate.now().minusYears(18)); // Exactly 18
        assertThat(user.isAdult()).isTrue();

        user.setDateOfBirth(LocalDate.now().minusYears(25)); // Over 18
        assertThat(user.isAdult()).isTrue();

        // Test status changes
        user.setStatus(UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();
        assertThat(user.isDeleted()).isFalse();

        user.setStatus(UserStatus.DELETED);
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.isActive()).isFalse();

        user.setStatus(UserStatus.SUSPENDED);
        assertThat(user.isActive()).isFalse();
        assertThat(user.isDeleted()).isFalse();

                // Test enum values for FEMALE gender and different statuses
        assertThat(user.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(user.getGender().getCode()).isEqualTo(Gender.FEMALE.getCode());
        assertThat(user.getGender().getDescription()).isEqualTo(Gender.FEMALE.getDescription());

        // Test status enum values
        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
        assertThat(user.getStatus().getCode()).isEqualTo(UserStatus.SUSPENDED.getCode());
        assertThat(user.getStatus().getDescription()).isEqualTo(UserStatus.SUSPENDED.getDescription());
    }

    @Test
    void testUserValidationEdgeCases() {
        // Test data variables
        String testExternalAuthId = "ext-auth-validation";
        String testFirstName = "Validation";
        String testLastName = "Test";
        String testEmail = "validation@gmail.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 5, 15);
        Gender testGender = Gender.FEMALE;
        UserRole testRole = UserRole.PATIENT;

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

                // Test canBeUpdated with different statuses
        assertThat(user.canBeUpdated()).isTrue(); // ACTIVE status

        user.setStatus(UserStatus.INACTIVE);
        assertThat(user.canBeUpdated()).isFalse(); // INACTIVE cannot be updated (not active)

        user.setStatus(UserStatus.SUSPENDED);
        assertThat(user.canBeUpdated()).isFalse(); // SUSPENDED cannot be updated (not active)

        user.setStatus(UserStatus.DELETED);
        assertThat(user.canBeUpdated()).isFalse(); // DELETED cannot be updated

        // Test null values
        user.setEmail(null);
        assertThat(user.hasValidHealthcareEmail()).isFalse();

        user.setPhone(null);
        assertThat(user.hasValidPhoneNumber()).isFalse();

        user.setDateOfBirth(null);
        assertThat(user.isAdult()).isFalse();
    }

}
