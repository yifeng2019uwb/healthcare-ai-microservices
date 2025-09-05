package com.healthcare.entity;

import com.healthcare.enums.ActionType;
import com.healthcare.enums.Gender;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.ResourceType;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AuditLog entity
 */
class AuditLogEntityTest {

    @Test
    void testAuditLogEntity() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        ResourceType testResourceType = ResourceType.PATIENT_PROFILE;
        Outcome testInitialOutcome = Outcome.SUCCESS;
        Outcome testUpdatedOutcome = Outcome.FAILURE;

        AuditLog auditLog = new AuditLog(testUserId, testActionType, testResourceType, testResourceId, testInitialOutcome);

        // Test basic properties
        assertThat(auditLog.getUserId()).isEqualTo(testUserId);
        assertThat(auditLog.getActionType()).isEqualTo(testActionType);
        assertThat(auditLog.getResourceType()).isEqualTo(testResourceType);
        assertThat(auditLog.getResourceId()).isEqualTo(testResourceId);
        assertThat(auditLog.getOutcome()).isEqualTo(testInitialOutcome);

        // Test that audit log is immutable - no setters available
        // AuditLog is immutable once created, so we can't change details or outcome
    }

    @Test
    void testAuditLogValidationMethods() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.UPDATE;
        ResourceType testResourceType = ResourceType.APPOINTMENT;
        Outcome testInitialOutcome = Outcome.SUCCESS;

        AuditLog auditLog = new AuditLog(testUserId, testActionType, testResourceType, testResourceId, testInitialOutcome);

        // Test basic field access
        assertThat(auditLog.getUserId()).isEqualTo(testUserId);
        assertThat(auditLog.getActionType()).isEqualTo(testActionType);
        assertThat(auditLog.getResourceType()).isEqualTo(testResourceType);
        assertThat(auditLog.getResourceId()).isEqualTo(testResourceId);
        assertThat(auditLog.getOutcome()).isEqualTo(Outcome.SUCCESS);

        // Test FAILURE outcome with a separate audit log
        AuditLog failureLog = new AuditLog(testUserId, ActionType.UPDATE, ResourceType.PATIENT_PROFILE, testResourceId, Outcome.FAILURE);
        assertThat(failureLog.getOutcome()).isEqualTo(Outcome.FAILURE);

        // Test source IP (audit logs are immutable, so we can't set these after creation)
        assertThat(auditLog.getSourceIp()).isNull();

        // Test user agent (audit logs are immutable, so we can't set these after creation)
        assertThat(auditLog.getUserAgent()).isNull();

        // Test details (audit logs are immutable, so we can't set these after creation)
        assertThat(auditLog.getDetails()).isNull();
    }

    @Test
    void testAuditLogActionTypes() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        Outcome testOutcome = Outcome.SUCCESS;

        // Test different action types
        AuditLog createLog = new AuditLog(testUserId, ActionType.CREATE, ResourceType.PATIENT_PROFILE, testResourceId, testOutcome);
        AuditLog readLog = new AuditLog(testUserId, ActionType.READ, ResourceType.APPOINTMENT, testResourceId, testOutcome);
        AuditLog updateLog = new AuditLog(testUserId, ActionType.UPDATE, ResourceType.MEDICAL_RECORD, testResourceId, testOutcome);
        AuditLog deleteLog = new AuditLog(testUserId, ActionType.DELETE, ResourceType.USER_PROFILE, testResourceId, testOutcome);

        assertThat(createLog.getActionType()).isEqualTo(ActionType.CREATE);
        assertThat(readLog.getActionType()).isEqualTo(ActionType.READ);
        assertThat(updateLog.getActionType()).isEqualTo(ActionType.UPDATE);
        assertThat(deleteLog.getActionType()).isEqualTo(ActionType.DELETE);
    }

    @Test
    void testAuditLogResourceTypes() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        Outcome testOutcome = Outcome.SUCCESS;

        // Test different resource types
        AuditLog userLog = new AuditLog(testUserId, testActionType, ResourceType.USER_PROFILE, testResourceId, testOutcome);
        AuditLog patientLog = new AuditLog(testUserId, testActionType, ResourceType.PATIENT_PROFILE, testResourceId, testOutcome);
        AuditLog providerLog = new AuditLog(testUserId, testActionType, ResourceType.PROVIDER_PROFILE, testResourceId, testOutcome);
        AuditLog appointmentLog = new AuditLog(testUserId, testActionType, ResourceType.APPOINTMENT, testResourceId, testOutcome);
        AuditLog medicalRecordLog = new AuditLog(testUserId, testActionType, ResourceType.MEDICAL_RECORD, testResourceId, testOutcome);

        assertThat(userLog.getResourceType()).isEqualTo(ResourceType.USER_PROFILE);
        assertThat(patientLog.getResourceType()).isEqualTo(ResourceType.PATIENT_PROFILE);
        assertThat(providerLog.getResourceType()).isEqualTo(ResourceType.PROVIDER_PROFILE);
        assertThat(appointmentLog.getResourceType()).isEqualTo(ResourceType.APPOINTMENT);
        assertThat(medicalRecordLog.getResourceType()).isEqualTo(ResourceType.MEDICAL_RECORD);
    }

    @Test
    void testAuditLogFullConstructor() throws Exception {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.UPDATE;
        ResourceType testResourceType = ResourceType.PATIENT_PROFILE;
        Outcome testOutcome = Outcome.SUCCESS;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testDetails = mapper.readTree("{\"field\": \"value\", \"oldValue\": \"old\", \"newValue\": \"new\"}");
        InetAddress testSourceIp = InetAddress.getByName("192.168.1.100");
        String testUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

        // Test full constructor with all fields
        AuditLog auditLog = new AuditLog(testUserId, testActionType, testResourceType, testResourceId,
                                       testOutcome, testDetails, testSourceIp, testUserAgent);

        // Verify all fields are set correctly
        assertThat(auditLog.getUserId()).isEqualTo(testUserId);
        assertThat(auditLog.getActionType()).isEqualTo(testActionType);
        assertThat(auditLog.getResourceType()).isEqualTo(testResourceType);
        assertThat(auditLog.getResourceId()).isEqualTo(testResourceId);
        assertThat(auditLog.getOutcome()).isEqualTo(testOutcome);
        assertThat(auditLog.getDetails()).isEqualTo(testDetails);
        assertThat(auditLog.getSourceIp()).isEqualTo(testSourceIp);
        assertThat(auditLog.getUserAgent()).isEqualTo(testUserAgent);
    }

    @Test
    void testAuditLogFullConstructorWithNulls() {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        ResourceType testResourceType = ResourceType.USER_PROFILE;
        Outcome testOutcome = Outcome.SUCCESS;

        // Test full constructor with null optional fields
        AuditLog auditLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                       testOutcome, null, null, null);

        // Verify required fields are set correctly
        assertThat(auditLog.getUserId()).isEqualTo(testUserId);
        assertThat(auditLog.getActionType()).isEqualTo(testActionType);
        assertThat(auditLog.getResourceType()).isEqualTo(testResourceType);
        assertThat(auditLog.getResourceId()).isNull();
        assertThat(auditLog.getOutcome()).isEqualTo(testOutcome);
        assertThat(auditLog.getDetails()).isNull();
        assertThat(auditLog.getSourceIp()).isNull();
        assertThat(auditLog.getUserAgent()).isNull();
    }

    @Test
    void testAuditLogUserAgentValidation() throws Exception {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        ResourceType testResourceType = ResourceType.USER_PROFILE;
        Outcome testOutcome = Outcome.SUCCESS;
        InetAddress testSourceIp = InetAddress.getByName("192.168.1.100");

        // Test valid user agent
        String validUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        AuditLog auditLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                       testOutcome, null, testSourceIp, validUserAgent);
        assertThat(auditLog.getUserAgent()).isEqualTo(validUserAgent);

        // Test trimmed user agent
        String trimmedUserAgent = "  Mozilla/5.0  ";
        AuditLog trimmedLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                         testOutcome, null, testSourceIp, trimmedUserAgent);
        assertThat(trimmedLog.getUserAgent()).isEqualTo("Mozilla/5.0");

        // Test empty user agent (should be normalized to null)
        AuditLog emptyLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                       testOutcome, null, testSourceIp, "");
        assertThat(emptyLog.getUserAgent()).isNull();

        // Test whitespace user agent (should be normalized to null)
        AuditLog whitespaceLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                            testOutcome, null, testSourceIp, "   ");
        assertThat(whitespaceLog.getUserAgent()).isNull();

        // Test user agent exceeding 500 characters - should throw ValidationException
        String longUserAgent = "a".repeat(501);
        assertThatThrownBy(() -> new AuditLog(testUserId, testActionType, testResourceType, null,
                                            testOutcome, null, testSourceIp, longUserAgent))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("User agent cannot exceed 500 characters");

        // Test user agent exactly 500 characters - should be valid
        String exactlyMaxUserAgent = "a".repeat(500);
        AuditLog maxLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                     testOutcome, null, testSourceIp, exactlyMaxUserAgent);
        assertThat(maxLog.getUserAgent()).isEqualTo(exactlyMaxUserAgent);
    }

    @Test
    void testAuditLogSourceIpValidation() throws Exception {
        // Test data variables
        UUID testUserId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        ResourceType testResourceType = ResourceType.USER_PROFILE;
        Outcome testOutcome = Outcome.SUCCESS;

        // Test valid IP address
        InetAddress validIp = InetAddress.getByName("192.168.1.100");
        AuditLog auditLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                       testOutcome, null, validIp, null);
        assertThat(auditLog.getSourceIp()).isEqualTo(validIp);

        // Test null IP address (should be allowed)
        AuditLog nullIpLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                        testOutcome, null, null, null);
        assertThat(nullIpLog.getSourceIp()).isNull();

        // Test loopback IP address - should be allowed (valid for development/testing)
        InetAddress loopbackIp = InetAddress.getByName("127.0.0.1");
        AuditLog loopbackLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                          testOutcome, null, loopbackIp, null);
        assertThat(loopbackLog.getSourceIp()).isEqualTo(loopbackIp);

        // Test multicast IP address - should throw ValidationException
        InetAddress multicastIp = InetAddress.getByName("224.0.0.1");
        assertThatThrownBy(() -> new AuditLog(testUserId, testActionType, testResourceType, null,
                                            testOutcome, null, multicastIp, null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Source IP cannot be a multicast address");

        // Test valid public IP address
        InetAddress publicIp = InetAddress.getByName("8.8.8.8");
        AuditLog publicIpLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                          testOutcome, null, publicIp, null);
        assertThat(publicIpLog.getSourceIp()).isEqualTo(publicIp);

        // Test valid private IP address
        InetAddress privateIp = InetAddress.getByName("10.0.0.1");
        AuditLog privateIpLog = new AuditLog(testUserId, testActionType, testResourceType, null,
                                           testOutcome, null, privateIp, null);
        assertThat(privateIpLog.getSourceIp()).isEqualTo(privateIp);
    }
}
