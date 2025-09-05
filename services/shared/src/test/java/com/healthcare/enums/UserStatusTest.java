package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserStatus enum
 */
class UserStatusTest {

    @Test
    void testUserStatusValues() {
        assertThat(UserStatus.values()).hasSize(4);
        assertThat(UserStatus.ACTIVE).isNotNull();
        assertThat(UserStatus.INACTIVE).isNotNull();
        assertThat(UserStatus.SUSPENDED).isNotNull();
        assertThat(UserStatus.DELETED).isNotNull();
    }

    @Test
    void testUserStatusCodes() {
        assertThat(UserStatus.ACTIVE.getCode()).isEqualTo("ACTIVE");
        assertThat(UserStatus.INACTIVE.getCode()).isEqualTo("INACTIVE");
        assertThat(UserStatus.SUSPENDED.getCode()).isEqualTo("SUSPENDED");
        assertThat(UserStatus.DELETED.getCode()).isEqualTo("DELETED");
    }

    @Test
    void testUserStatusDescriptions() {
        assertThat(UserStatus.ACTIVE.getDescription()).isEqualTo("Active user");
        assertThat(UserStatus.INACTIVE.getDescription()).isEqualTo("Inactive user");
        assertThat(UserStatus.SUSPENDED.getDescription()).isEqualTo("Suspended user");
        assertThat(UserStatus.DELETED.getDescription()).isEqualTo("Deleted user");
    }

    @Test
    void testUserStatusValueOf() {
        assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
        assertThat(UserStatus.valueOf("INACTIVE")).isEqualTo(UserStatus.INACTIVE);
        assertThat(UserStatus.valueOf("SUSPENDED")).isEqualTo(UserStatus.SUSPENDED);
        assertThat(UserStatus.valueOf("DELETED")).isEqualTo(UserStatus.DELETED);
    }
}
