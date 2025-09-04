package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Provider entity
 */
class ProviderEntityTest {

    @Test
    void testProviderEntity() {
        // Test data variables
        String testExternalAuthId = "ext-auth-789";
        String testFirstName = "Dr. Bob";
        String testLastName = "Johnson";
        String testEmail = "dr.bob@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1980, 3, 20);
        Gender testGender = Gender.MALE;
        UserRole testRole = UserRole.PROVIDER;
        String testNpiNumber = "1234567890";
        String testSpecialty = "Cardiology";
        String testLicenseNumbers = "MD123456";
        String testQualifications = "MD, PhD in Cardiology";
        String testBio = "Experienced cardiologist";
        String testOfficePhone = "+15555678901";
        String testCustomData = "{\"rating\": 4.8}";

        // Create user first
        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        // Create provider
        Provider provider = new Provider(user, testNpiNumber);
        provider.setSpecialty(testSpecialty);
        provider.setLicenseNumbers(testLicenseNumbers);
        provider.setQualifications(testQualifications);

        // Test basic properties
        assertThat(provider.getUser()).isEqualTo(user);
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
        assertThat(provider.getCustomData()).isEqualTo(testCustomData);
    }

    @Test
    void testProviderValidationMethods() {
        // Test data variables
        String testExternalAuthId = "ext-auth-provider";
        String testFirstName = "Dr. Test";
        String testLastName = "Provider";
        String testEmail = "test@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1980, 3, 20);
        Gender testGender = Gender.MALE;
        UserRole testRole = UserRole.PROVIDER;
        String testNpiNumber = "9876543210";
        String testSpecialty = "Internal Medicine";
        String testLicenseNumbers = "MD987654";
        String testQualifications = "MD, Board Certified";
        String testOfficePhone = "+15551234567";

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        Provider provider = new Provider(user, testNpiNumber);

        // Test validation methods
        assertThat(provider.hasValidNpiNumber()).isTrue();
        assertThat(provider.isReadyForAppointments()).isFalse(); // No specialty yet

        // Test specialty validation
        provider.setSpecialty(testSpecialty);
        assertThat(provider.hasSpecialty()).isTrue();
        assertThat(provider.isReadyForAppointments()).isTrue();

        // Test license validation
        provider.setLicenseNumbers(testLicenseNumbers);
        assertThat(provider.hasLicenseInfo()).isTrue();

        // Test qualifications validation
        provider.setQualifications(testQualifications);
        assertThat(provider.hasQualifications()).isTrue();

        // Test office phone validation
        provider.setOfficePhone(testOfficePhone);
        assertThat(provider.hasOfficePhone()).isTrue();

        // Test complete professional info
        assertThat(provider.hasCompleteProfessionalInfo()).isTrue();
    }
}
