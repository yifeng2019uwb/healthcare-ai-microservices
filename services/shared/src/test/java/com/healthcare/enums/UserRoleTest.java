package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserRole enum
 */
class UserRoleTest {

    @Test
    void testUserRoleValues() {
        assertThat(UserRole.values()).hasSize(2);
        assertThat(UserRole.PATIENT).isNotNull();
        assertThat(UserRole.PROVIDER).isNotNull();
    }

    @Test
    void testUserRoleCodes() {
        assertThat(UserRole.PATIENT.getCode()).isEqualTo("PATIENT");
        assertThat(UserRole.PROVIDER.getCode()).isEqualTo("PROVIDER");
    }

    @Test
    void testUserRoleDescriptions() {
        assertThat(UserRole.PATIENT.getDescription()).isEqualTo("Patient user");
        assertThat(UserRole.PROVIDER.getDescription()).isEqualTo("Healthcare provider");
    }

    @Test
    void testUserRoleValueOf() {
        assertThat(UserRole.valueOf("PATIENT")).isEqualTo(UserRole.PATIENT);
        assertThat(UserRole.valueOf("PROVIDER")).isEqualTo(UserRole.PROVIDER);
    }
}
