package com.healthcare.entity;

import com.healthcare.enums.UserRole;
import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link User} — auth-only profile (username, email, password hash, role, active flag).
 */
class UserEntityTest {

    private static final String USERNAME = "john_doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD_HASH = "$2a$10$abcdefghijklmnopqrstuv";

    private User newUser(UserRole role) {
        return new User(USERNAME, EMAIL, PASSWORD_HASH, role);
    }

    @Test
    void constructor_setsFieldsAndDefaultsActive() {
        User user = newUser(UserRole.PATIENT);

        assertThat(user.getUsername()).isEqualTo(USERNAME);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPasswordHash()).isEqualTo(PASSWORD_HASH);
        assertThat(user.getRole()).isEqualTo(UserRole.PATIENT);
        assertThat(user.getIsActive()).isTrue();
        assertThat(user.isActive()).isTrue();
        assertThat(user.isPatient()).isTrue();
        assertThat(user.isProvider()).isFalse();
    }

    @Test
    void isProvider_whenRoleProvider() {
        User user = newUser(UserRole.PROVIDER);
        assertThat(user.isProvider()).isTrue();
        assertThat(user.isPatient()).isFalse();
    }

    @Test
    void constructor_trimsUsernameAndEmail() {
        User user = new User("  jane  ", "  jane@example.com  ", PASSWORD_HASH, UserRole.PATIENT);
        assertThat(user.getUsername()).isEqualTo("jane");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    void constructor_validation() {
        assertThatThrownBy(() -> new User(null, EMAIL, PASSWORD_HASH, UserRole.PATIENT))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Username is required");
        assertThatThrownBy(() -> new User("", EMAIL, PASSWORD_HASH, UserRole.PATIENT))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Username is required");
        assertThatThrownBy(() -> new User(USERNAME, null, PASSWORD_HASH, UserRole.PATIENT))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Email is required");
        assertThatThrownBy(() -> new User(USERNAME, EMAIL, null, UserRole.PATIENT))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password hash cannot be blank");
        assertThatThrownBy(() -> new User(USERNAME, EMAIL, "", UserRole.PATIENT))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password hash cannot be blank");
        assertThatThrownBy(() -> new User(USERNAME, EMAIL, PASSWORD_HASH, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Role is required");
    }

    @Test
    void setPasswordHash() {
        User user = newUser(UserRole.PATIENT);
        user.setPasswordHash("$2a$10$newhashhere");
        assertThat(user.getPasswordHash()).isEqualTo("$2a$10$newhashhere");

        assertThatThrownBy(() -> user.setPasswordHash(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password hash cannot be blank");
        assertThatThrownBy(() -> user.setPasswordHash("  "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Password hash cannot be blank");
    }

    @Test
    void setIsActive() {
        User user = newUser(UserRole.PATIENT);
        user.setIsActive(false);
        assertThat(user.getIsActive()).isFalse();
        assertThat(user.isActive()).isFalse();

        assertThatThrownBy(() -> user.setIsActive(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("isActive cannot be null");
    }
}
