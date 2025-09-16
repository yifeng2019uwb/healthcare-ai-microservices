package com.healthcare.entity;

import com.healthcare.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for Provider entity
 */
class ProviderEntityTest {

    @Test
    void testProviderEntity() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        String testSpecialty = "Cardiology";
        String testLicenseNumbers = "MD123456";
        String testQualifications = "MD, PhD in Cardiology";
        String testBio = "Experienced cardiologist";
        String testOfficePhone = "+15555678901";
        JsonNode testCustomData = null; // Will be set directly as JsonNode

        // Create provider
        Provider provider = new Provider(testUserId, testNpiNumber);
        provider.setSpecialty(testSpecialty);
        provider.setLicenseNumbers(testLicenseNumbers);
        provider.setQualifications(testQualifications);

        // Test basic properties
        assertThat(provider.getUserId()).isEqualTo(testUserId);
        assertThat(provider.getNpiNumber()).isEqualTo(testNpiNumber);
        assertThat(provider.getSpecialty()).isEqualTo(testSpecialty);
        assertThat(provider.getLicenseNumbers()).isEqualTo(testLicenseNumbers);
        assertThat(provider.getQualifications()).isEqualTo(testQualifications);

        // Test additional setters
        provider.setBio(testBio);
        provider.setOfficePhone(testOfficePhone);
        provider.setCustomData(testCustomData);

        assertThat(provider.getBio()).isEqualTo(testBio);
        assertThat(provider.getOfficePhone()).isEqualTo(testOfficePhone);
        assertThat(provider.getCustomData()).isNull();
    }

    @Test
    void testProviderValidationMethods() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "9876543210";
        String testSpecialty = "Internal Medicine";
        String testLicenseNumbers = "MD987654";
        String testQualifications = "MD, Board Certified";
        String testOfficePhone = "+15551234567";

        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test basic field access
        assertThat(provider.getUserId()).isEqualTo(testUserId);
        assertThat(provider.getNpiNumber()).isEqualTo(testNpiNumber);

        // Test specialty field
        assertThat(provider.getSpecialty()).isNull();
        provider.setSpecialty(testSpecialty);
        assertThat(provider.getSpecialty()).isEqualTo(testSpecialty);

        // Test license field
        assertThat(provider.getLicenseNumbers()).isNull();
        provider.setLicenseNumbers(testLicenseNumbers);
        assertThat(provider.getLicenseNumbers()).isEqualTo(testLicenseNumbers);

        // Test qualifications field
        assertThat(provider.getQualifications()).isNull();
        provider.setQualifications(testQualifications);
        assertThat(provider.getQualifications()).isEqualTo(testQualifications);

        // Test office phone field
        assertThat(provider.getOfficePhone()).isNull();
        provider.setOfficePhone(testOfficePhone);
        assertThat(provider.getOfficePhone()).isEqualTo(testOfficePhone);
    }

    @Test
    void testLicenseNumbersField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test setter with valid value
        String testLicenseNumbers = "MD123456";
        provider.setLicenseNumbers(testLicenseNumbers);
        assertThat(provider.getLicenseNumbers()).isEqualTo(testLicenseNumbers);

        // Test setter with null value (allowed)
        provider.setLicenseNumbers(null);
        assertThat(provider.getLicenseNumbers()).isNull();

        // Test setter with empty value (normalized to null)
        provider.setLicenseNumbers("");
        assertThat(provider.getLicenseNumbers()).isNull();

        // Test setter validation - too long
        String longLicenseNumbers = "A".repeat(51);
        assertThatThrownBy(() -> provider.setLicenseNumbers(longLicenseNumbers))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("License numbers cannot exceed 50 characters");
    }

    @Test
    void testNpiNumberField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test getter - npiNumber is immutable after creation
        assertThat(provider.getNpiNumber()).isEqualTo(testNpiNumber);
    }

    @Test
    void testSpecialtyField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test setter with valid value
        String testSpecialty = "Cardiology";
        provider.setSpecialty(testSpecialty);
        assertThat(provider.getSpecialty()).isEqualTo(testSpecialty);

        // Test setter with null value (allowed)
        provider.setSpecialty(null);
        assertThat(provider.getSpecialty()).isNull();

        // Test setter with empty value (normalized to null)
        provider.setSpecialty("");
        assertThat(provider.getSpecialty()).isNull();

        // Test setter validation - too long
        String longSpecialty = "A".repeat(101);
        assertThatThrownBy(() -> provider.setSpecialty(longSpecialty))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Specialty cannot exceed 100 characters");
    }

    @Test
    void testQualificationsField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test setter with valid value
        String testQualifications = "MD, PhD in Cardiology";
        provider.setQualifications(testQualifications);
        assertThat(provider.getQualifications()).isEqualTo(testQualifications);

        // Test setter with null value (allowed)
        provider.setQualifications(null);
        assertThat(provider.getQualifications()).isNull();

        // Test setter with empty value (normalized to null)
        provider.setQualifications("");
        assertThat(provider.getQualifications()).isNull();
    }

    @Test
    void testBioField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test setter with valid value
        String testBio = "Experienced cardiologist with 20 years of practice";
        provider.setBio(testBio);
        assertThat(provider.getBio()).isEqualTo(testBio);

        // Test setter with null value (allowed)
        provider.setBio(null);
        assertThat(provider.getBio()).isNull();

        // Test setter with empty value (normalized to null)
        provider.setBio("");
        assertThat(provider.getBio()).isNull();
    }

    @Test
    void testOfficePhoneField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test setter with valid value
        String testOfficePhone = "+15551234567";
        provider.setOfficePhone(testOfficePhone);
        assertThat(provider.getOfficePhone()).isEqualTo(testOfficePhone);

        // Test setter with null value (allowed)
        provider.setOfficePhone(null);
        assertThat(provider.getOfficePhone()).isNull();

        // Test setter with empty value (normalized to null)
        provider.setOfficePhone("");
        assertThat(provider.getOfficePhone()).isNull();

        // Test setter validation - too long
        String longOfficePhone = "+123456789012345678901";
        assertThatThrownBy(() -> provider.setOfficePhone(longOfficePhone))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Office phone cannot exceed 20 characters");

        // Test setter validation - invalid format
        assertThatThrownBy(() -> provider.setOfficePhone("0123456789"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Office phone must be a valid international format");
    }

    @Test
    void testCustomDataField() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Test setter with null value (allowed)
        provider.setCustomData(null);
        assertThat(provider.getCustomData()).isNull();

        // Note: JsonNode creation and validation should be handled at service layer
        // Entity only accepts pre-validated JsonNode objects
    }

    @Test
    void testGetUser() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Initially should be null
        assertThat(provider.getUser()).isNull();

        // Create a user and set it (using reflection since there's no setter)
        User user = new User("auth_123", "test@example.com", com.healthcare.enums.UserRole.PROVIDER);
        try {
            java.lang.reflect.Field userField = Provider.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(provider, user);
        } catch (Exception e) {
            fail("Failed to set user field via reflection: " + e.getMessage());
        }

        // Now should return the user
        assertThat(provider.getUser()).isEqualTo(user);
    }

    @Test
    void testGetAppointments() {
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";
        Provider provider = new Provider(testUserId, testNpiNumber);

        // Initially should return empty list (not null)
        assertThat(provider.getAppointments()).isNotNull();
        assertThat(provider.getAppointments()).isEmpty();

        // Create an appointment and add it to the list
        Appointment appointment = new Appointment(UUID.randomUUID(), java.time.OffsetDateTime.now().plusDays(2), com.healthcare.enums.AppointmentType.REGULAR_CONSULTATION);
        provider.getAppointments().add(appointment);

        // Now should contain the appointment
        assertThat(provider.getAppointments()).hasSize(1);
        assertThat(provider.getAppointments()).contains(appointment);
    }

    @Test
    void testConstructorValidation() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";

        // Test null userId
        assertThatThrownBy(() -> new Provider(null, testNpiNumber))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User ID is required");

        // Test null npiNumber
        assertThatThrownBy(() -> new Provider(testUserId, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("NPI number is required");

        // Test empty npiNumber
        assertThatThrownBy(() -> new Provider(testUserId, ""))
                .isInstanceOf(ValidationException.class)
                .hasMessage("NPI number is required");

        // Test whitespace npiNumber
        assertThatThrownBy(() -> new Provider(testUserId, "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("NPI number is required");
    }

    @Test
    void testValidateState() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";

        // Test valid provider - should not throw exception
        Provider validProvider = new Provider(testUserId, testNpiNumber);
        assertThatCode(() -> validProvider.validateState()).doesNotThrowAnyException();

        // Test with null userId
        final Provider provider1 = new Provider(testUserId, testNpiNumber);
        try {
            java.lang.reflect.Field field = Provider.class.getDeclaredField("userId");
            field.setAccessible(true);
            field.set(provider1, null);
        } catch (Exception e) {
            fail("Failed to set userId field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> provider1.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("User ID is required");

        // Test with null npiNumber
        final Provider provider2 = new Provider(testUserId, testNpiNumber);
        try {
            java.lang.reflect.Field field = Provider.class.getDeclaredField("npiNumber");
            field.setAccessible(true);
            field.set(provider2, null);
        } catch (Exception e) {
            fail("Failed to set npiNumber field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> provider2.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("NPI number is required");

        // Test with empty npiNumber
        final Provider provider3 = new Provider(testUserId, testNpiNumber);
        try {
            java.lang.reflect.Field field = Provider.class.getDeclaredField("npiNumber");
            field.setAccessible(true);
            field.set(provider3, "");
        } catch (Exception e) {
            fail("Failed to set npiNumber field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> provider3.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("NPI number is required");

        // Test with invalid npiNumber format (too short)
        final Provider provider4 = new Provider(testUserId, testNpiNumber);
        try {
            java.lang.reflect.Field field = Provider.class.getDeclaredField("npiNumber");
            field.setAccessible(true);
            field.set(provider4, "123"); // "123" is too short for NPI pattern
        } catch (Exception e) {
            fail("Failed to set npiNumber field via reflection: " + e.getMessage());
        }
        assertThatThrownBy(() -> provider4.validateState())
                .isInstanceOf(ValidationException.class)
                .hasMessage("NPI number must be exactly 10 digits");
    }

    @Test
    void testHasCompleteCredentials() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        String testNpiNumber = "1234567890";

        // Test provider with no credentials - should return false
        Provider provider = new Provider(testUserId, testNpiNumber);
        assertThat(provider.hasCompleteCredentials()).isFalse();

        // Test provider with only specialty - should return false
        provider.setSpecialty("Cardiology");
        assertThat(provider.hasCompleteCredentials()).isFalse();

        // Test provider with specialty and license numbers - should return false
        provider.setLicenseNumbers("MD123456");
        assertThat(provider.hasCompleteCredentials()).isFalse();

        // Test provider with specialty, license numbers, and qualifications - should return true
        provider.setQualifications("Board Certified Cardiologist");
        assertThat(provider.hasCompleteCredentials()).isTrue();

        // Test provider with empty specialty - should return false
        provider.setSpecialty("");
        assertThat(provider.hasCompleteCredentials()).isFalse();

        // Test provider with empty license numbers - should return false
        provider.setSpecialty("Cardiology");
        provider.setLicenseNumbers("");
        assertThat(provider.hasCompleteCredentials()).isFalse();

        // Test provider with empty qualifications - should return false
        provider.setLicenseNumbers("MD123456");
        provider.setQualifications("");
        assertThat(provider.hasCompleteCredentials()).isFalse();

        // Test provider with whitespace-only credentials - should return false
        provider.setSpecialty("   ");
        provider.setLicenseNumbers("   ");
        provider.setQualifications("   ");
        assertThat(provider.hasCompleteCredentials()).isFalse();
    }
}
