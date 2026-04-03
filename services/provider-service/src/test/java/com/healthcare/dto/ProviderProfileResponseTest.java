package com.healthcare.dto;

import com.healthcare.entity.Provider;
import com.healthcare.enums.Gender;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderProfileResponseTest {

    @Test
    void from_mapsAllFields() {
        Provider provider = new Provider(UUID.randomUUID(), "Dr. Smith");
        provider.setGender(Gender.M);
        provider.setSpeciality("Cardiology");
        provider.setPhone("+15551234567");
        provider.setLicenseNumber("LIC-12345");
        provider.setBio("Experienced cardiologist");

        ProviderProfileResponse response = ProviderProfileResponse.from(provider);

        assertThat(response.name()).isEqualTo("Dr. Smith");
        assertThat(response.gender()).isEqualTo("M");
        assertThat(response.speciality()).isEqualTo("Cardiology");
        assertThat(response.phone()).isEqualTo("+15551234567");
        assertThat(response.licenseNumber()).isEqualTo("LIC-12345");
        assertThat(response.bio()).isEqualTo("Experienced cardiologist");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void from_handlesNullGender() {
        Provider provider = new Provider(UUID.randomUUID(), "Dr. Jones");

        ProviderProfileResponse response = ProviderProfileResponse.from(provider);

        assertThat(response.gender()).isNull();
    }

    @Test
    void from_handlesNullOrganization() {
        Provider provider = new Provider(UUID.randomUUID(), "Dr. Lee");

        ProviderProfileResponse response = ProviderProfileResponse.from(provider);

        assertThat(response.organization()).isNull();
    }

    @Test
    void from_handlesNullOptionalFields() {
        Provider provider = new Provider(UUID.randomUUID(), "Dr. Minimal");

        ProviderProfileResponse response = ProviderProfileResponse.from(provider);

        assertThat(response.speciality()).isNull();
        assertThat(response.phone()).isNull();
        assertThat(response.licenseNumber()).isNull();
        assertThat(response.bio()).isNull();
        assertThat(response.providerCode()).isNull();
    }
}
