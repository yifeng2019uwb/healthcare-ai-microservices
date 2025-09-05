package com.healthcare.entity;

import com.healthcare.enums.Gender;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        String testCustomData = "{\"rating\": 4.8}";

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
        assertThat(provider.getCustomData()).isEqualTo(testCustomData);
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
}
