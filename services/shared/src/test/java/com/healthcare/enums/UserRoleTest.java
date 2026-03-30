package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserRole enum
 */
class UserRoleTest {

    @Test
    void testUserRoleValues() {
        assertThat(UserRole.values()).hasSize(3);
        assertThat(UserRole.PATIENT).isNotNull();
        assertThat(UserRole.PROVIDER).isNotNull();
        assertThat(UserRole.ADMIN).isNotNull();
    }

    @Test
    void testUserRoleValueOf() {
        assertThat(UserRole.valueOf("PATIENT")).isEqualTo(UserRole.PATIENT);
        assertThat(UserRole.valueOf("PROVIDER")).isEqualTo(UserRole.PROVIDER);
        assertThat(UserRole.valueOf("ADMIN")).isEqualTo(UserRole.ADMIN);
    }
}
