package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResourceType enum
 */
class ResourceTypeTest {

    @Test
    void testResourceTypeValues() {
        assertThat(ResourceType.values()).hasSize(5);
        assertThat(ResourceType.USER_PROFILE).isNotNull();
        assertThat(ResourceType.PATIENT_PROFILE).isNotNull();
        assertThat(ResourceType.PROVIDER_PROFILE).isNotNull();
        assertThat(ResourceType.APPOINTMENT).isNotNull();
        assertThat(ResourceType.MEDICAL_RECORD).isNotNull();
    }

    @Test
    void testResourceTypeCodes() {
        assertThat(ResourceType.USER_PROFILE.getCode()).isEqualTo("USER_PROFILE");
        assertThat(ResourceType.PATIENT_PROFILE.getCode()).isEqualTo("PATIENT_PROFILE");
        assertThat(ResourceType.PROVIDER_PROFILE.getCode()).isEqualTo("PROVIDER_PROFILE");
        assertThat(ResourceType.APPOINTMENT.getCode()).isEqualTo("APPOINTMENT");
        assertThat(ResourceType.MEDICAL_RECORD.getCode()).isEqualTo("MEDICAL_RECORD");
    }

    @Test
    void testResourceTypeDescriptions() {
        assertThat(ResourceType.USER_PROFILE.getDescription()).isEqualTo("User profile");
        assertThat(ResourceType.PATIENT_PROFILE.getDescription()).isEqualTo("Patient profile");
        assertThat(ResourceType.PROVIDER_PROFILE.getDescription()).isEqualTo("Provider profile");
        assertThat(ResourceType.APPOINTMENT.getDescription()).isEqualTo("Appointment");
        assertThat(ResourceType.MEDICAL_RECORD.getDescription()).isEqualTo("Medical record");
    }

    @Test
    void testResourceTypeValueOf() {
        assertThat(ResourceType.valueOf("USER_PROFILE")).isEqualTo(ResourceType.USER_PROFILE);
        assertThat(ResourceType.valueOf("PATIENT_PROFILE")).isEqualTo(ResourceType.PATIENT_PROFILE);
        assertThat(ResourceType.valueOf("PROVIDER_PROFILE")).isEqualTo(ResourceType.PROVIDER_PROFILE);
        assertThat(ResourceType.valueOf("APPOINTMENT")).isEqualTo(ResourceType.APPOINTMENT);
        assertThat(ResourceType.valueOf("MEDICAL_RECORD")).isEqualTo(ResourceType.MEDICAL_RECORD);
    }
}
