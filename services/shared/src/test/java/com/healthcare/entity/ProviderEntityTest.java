package com.healthcare.entity;

import com.healthcare.enums.UserRole;
import com.healthcare.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for {@link Provider} — Synthea + registration fields (organization, name, auth link).
 */
class ProviderEntityTest {

    private static final UUID ORG_ID = UUID.randomUUID();

    private Provider newProvider() {
        return new Provider(ORG_ID, "Dr. Jane Smith");
    }

    @Test
    void constructor_setsOrganizationNameAndDefaults() {
        Provider p = newProvider();
        assertThat(p.getOrganizationId()).isEqualTo(ORG_ID);
        assertThat(p.getName()).isEqualTo("Dr. Jane Smith");
        assertThat(p.getIsActive()).isTrue();
        assertThat(p.isActive()).isTrue();
        assertThat(p.getAuthId()).isNull();
        assertThat(p.getProviderCode()).isNull();
        assertThat(p.getUser()).isNull();
        assertThat(p.isRegistered()).isFalse();
    }

    @Test
    void constructor_trimsName() {
        Provider p = new Provider(ORG_ID, "  Alice  ");
        assertThat(p.getName()).isEqualTo("Alice");
    }

    @Test
    void constructor_validation() {
        assertThatThrownBy(() -> new Provider(null, "Name"))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Organization ID is required");
        assertThatThrownBy(() -> new Provider(ORG_ID, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Provider name is required");
        assertThatThrownBy(() -> new Provider(ORG_ID, "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Provider name is required");
    }

    @Test
    void linkAuthAccount_andIsRegistered() {
        Provider p = newProvider();
        UUID userId = UUID.randomUUID();
        p.linkAuthAccount(userId);
        assertThat(p.getAuthId()).isEqualTo(userId);
        assertThat(p.isRegistered()).isTrue();

        assertThatThrownBy(() -> p.linkAuthAccount(UUID.randomUUID()))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Provider is already linked to an account");
    }

    @Test
    void linkAuthAccount_rejectsNull() {
        assertThatThrownBy(() -> newProvider().linkAuthAccount(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("User ID cannot be null");
    }

    @Test
    void matchesRegistrationCredentials() throws Exception {
        Provider p = newProvider();
        var codeField = Provider.class.getDeclaredField("providerCode");
        codeField.setAccessible(true);
        codeField.set(p, "PRV-000042");

        assertThat(p.matchesRegistrationCredentials("prv-000042", "Dr. Jane Smith")).isTrue();
        assertThat(p.matchesRegistrationCredentials("PRV-000042", "other")).isFalse();
        assertThat(p.matchesRegistrationCredentials(null, "Dr. Jane Smith")).isFalse();
        assertThat(p.matchesRegistrationCredentials("PRV-000042", null)).isFalse();
    }

    @Test
    void setName() {
        Provider p = newProvider();
        p.setName("Dr. Bob");
        assertThat(p.getName()).isEqualTo("Dr. Bob");
        p.setName("   ");
        assertThat(p.getName()).isNull();
    }

    @Test
    void setPhone() {
        Provider p = newProvider();
        p.setPhone("+15551234567");
        assertThat(p.getPhone()).isEqualTo("+15551234567");

        assertThatThrownBy(() -> p.setPhone("0123456789"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone must be a valid international format");
    }

    @Test
    void setLicenseNumber() {
        Provider p = newProvider();
        p.setLicenseNumber("MD12345");
        assertThat(p.getLicenseNumber()).isEqualTo("MD12345");
        p.setLicenseNumber("");
        assertThat(p.getLicenseNumber()).isNull();
    }

    @Test
    void setBio() {
        Provider p = newProvider();
        p.setBio("Board certified");
        assertThat(p.getBio()).isEqualTo("Board certified");
    }

    @Test
    void setIsActive() {
        Provider p = newProvider();
        p.setIsActive(false);
        assertThat(p.isActive()).isFalse();
        assertThatThrownBy(() -> p.setIsActive(null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("isActive cannot be null");
    }

    @Test
    void getUser_viaReflection() {
        Provider p = newProvider();
        User user = new User("prov1", "p@example.com", "$2a$10$hash", UserRole.PROVIDER);
        try {
            var f = Provider.class.getDeclaredField("user");
            f.setAccessible(true);
            f.set(p, user);
        } catch (Exception e) {
            fail("reflection: " + e.getMessage());
        }
        assertThat(p.getUser()).isEqualTo(user);
    }
}
