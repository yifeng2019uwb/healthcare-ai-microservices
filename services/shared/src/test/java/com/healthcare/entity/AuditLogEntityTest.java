package com.healthcare.entity;

import com.healthcare.enums.ActionType;
import com.healthcare.enums.Gender;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.ResourceType;
import com.healthcare.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        // Test validation methods
        assertThat(auditLog.hasUser()).isTrue();
        assertThat(auditLog.hasActionType()).isTrue();
        assertThat(auditLog.hasResourceType()).isTrue();
        assertThat(auditLog.hasResourceId()).isTrue();
        assertThat(auditLog.hasOutcome()).isTrue();
        assertThat(auditLog.isComplete()).isTrue();

        // Test outcome validations (audit logs are immutable once created)
        assertThat(auditLog.isSuccessful()).isTrue();
        assertThat(auditLog.isFailed()).isFalse();
        assertThat(auditLog.getOutcome()).isEqualTo(Outcome.SUCCESS);

        // Test FAILURE outcome with a separate audit log
        AuditLog failureLog = new AuditLog(testUserId, ActionType.UPDATE, ResourceType.PATIENT_PROFILE, testResourceId, Outcome.FAILURE);
        assertThat(failureLog.isSuccessful()).isFalse();
        assertThat(failureLog.isFailed()).isTrue();
        assertThat(failureLog.getOutcome()).isEqualTo(Outcome.FAILURE);

        // Test source IP (audit logs are immutable, so we can't set these after creation)
        assertThat(auditLog.hasSourceIp()).isFalse();
        assertThat(auditLog.getSourceIp()).isNull();

        // Test user agent (audit logs are immutable, so we can't set these after creation)
        assertThat(auditLog.hasUserAgent()).isFalse();
        assertThat(auditLog.getUserAgent()).isNull();
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
}
