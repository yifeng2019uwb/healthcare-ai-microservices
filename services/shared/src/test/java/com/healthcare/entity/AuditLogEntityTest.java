package com.healthcare.entity;

import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AuditLog} — append-only audit row with string action/resource/outcome.
 */
class AuditLogEntityTest {

    private AuditLog createLog() {
        return new AuditLog(
                ActionType.CREATE,
                "Patient_Profile",
                Outcome.SUCCESS);
    }

    @Test
    void constructor_setsCoreFields() {
        AuditLog log = createLog();
        assertThat(log.getAction()).isEqualTo(ActionType.CREATE);
        assertThat(log.getResourceType()).isEqualTo("Patient_Profile");
        assertThat(log.getOutcome()).isEqualTo(Outcome.SUCCESS);
        assertThat(log.getAuthId()).isNull();
        assertThat(log.getUserRole()).isNull();
        assertThat(log.getResourceId()).isNull();
        assertThat(log.getSourceIp()).isNull();
        assertThat(log.getUserAgent()).isNull();
        assertThat(log.getCreatedAt()).isNull();
        assertThat(log.getId()).isNull();
    }

    @Test
    void fluentWithMethods_chain() throws Exception {
        UUID resourceId = UUID.randomUUID();
        InetAddress ip = InetAddress.getByName("192.168.1.10");
        String auth_id = "jwt-subject-123";
        String userAgent = "Mozilla/5.0";

        AuditLog log = new AuditLog(
                ActionType.READ,
                "user_Profile",
                Outcome.FAILURE)
                .withAuthId(auth_id)
                .withUserRole(UserRole.PATIENT)
                .withResourceId(resourceId)
                .withSourceIp(ip)
                .withUserAgent(userAgent);

        assertThat(log.getAuthId()).isEqualTo(auth_id);
        assertThat(log.getUserRole()).isEqualTo(UserRole.PATIENT);
        assertThat(log.getResourceId()).isEqualTo(resourceId);
        assertThat(log.getSourceIp()).isEqualTo(ip);
        assertThat(log.getUserAgent()).isEqualTo(userAgent);
    }

    @Test
    void resourceTypes_roundTripCodes() {
        String type = "Encounter";

        assertThat(new AuditLog(
                ActionType.UPDATE,
                type,
                Outcome.SUCCESS).getResourceType()).isEqualTo(type);
    }
}
