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
        String testExternalAuthId = "ext-auth-audit";
        String testFirstName = "Audit";
        String testLastName = "User";
        String testEmail = "audit@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testGender = Gender.OTHER;
        UserRole testRole = UserRole.PATIENT;

        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        ResourceType testResourceType = ResourceType.PATIENT_PROFILE;
        Outcome testInitialOutcome = Outcome.SUCCESS;
        Outcome testUpdatedOutcome = Outcome.FAILURE;
        String testInitialDetails = "{\"action\": \"Patient record created\"}";
        String testUpdatedDetails = "{\"action\": \"Patient record updated\"}";
        String testUserAgent = "Mozilla/5.0";

        // Create a user first
        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        AuditLog auditLog = new AuditLog(user, testActionType, testResourceType, testResourceId, testInitialOutcome);
        auditLog.setDetails(testInitialDetails);
        auditLog.setUserAgent(testUserAgent);

        // Test basic properties
        assertThat(auditLog.getUser()).isEqualTo(user);
        assertThat(auditLog.getActionType()).isEqualTo(testActionType);
        assertThat(auditLog.getResourceType()).isEqualTo(testResourceType);
        assertThat(auditLog.getResourceId()).isEqualTo(testResourceId);
        assertThat(auditLog.getOutcome()).isEqualTo(testInitialOutcome);
        assertThat(auditLog.getDetails()).isEqualTo(testInitialDetails);
        assertThat(auditLog.getUserAgent()).isEqualTo(testUserAgent);

        // Test additional setters
        auditLog.setDetails(testUpdatedDetails);
        auditLog.setOutcome(testUpdatedOutcome);

        assertThat(auditLog.getDetails()).isEqualTo(testUpdatedDetails);
        assertThat(auditLog.getOutcome()).isEqualTo(testUpdatedOutcome);
    }

    @Test
    void testAuditLogValidationMethods() {
        // Test data variables
        String testExternalAuthId = "ext-auth-audit2";
        String testFirstName = "Audit";
        String testLastName = "User2";
        String testEmail = "audit2@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testGender = Gender.OTHER;
        UserRole testRole = UserRole.PATIENT;

        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.UPDATE;
        ResourceType testResourceType = ResourceType.APPOINTMENT;
        Outcome testInitialOutcome = Outcome.SUCCESS;
        Outcome testUpdatedOutcome = Outcome.FAILURE;
        String testUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";
        String testSourceIp = "192.168.1.1";

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        AuditLog auditLog = new AuditLog(user, testActionType, testResourceType, testResourceId, testInitialOutcome);

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
        String testExternalAuthId = "ext-auth-audit3";
        String testFirstName = "Audit";
        String testLastName = "User3";
        String testEmail = "audit3@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testGender = Gender.OTHER;
        UserRole testRole = UserRole.PATIENT;

        UUID testResourceId = UUID.randomUUID();
        Outcome testOutcome = Outcome.SUCCESS;

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        // Test different action types
        AuditLog createLog = new AuditLog(user, ActionType.CREATE, ResourceType.PATIENT_PROFILE, testResourceId, testOutcome);
        AuditLog readLog = new AuditLog(user, ActionType.READ, ResourceType.APPOINTMENT, testResourceId, testOutcome);
        AuditLog updateLog = new AuditLog(user, ActionType.UPDATE, ResourceType.MEDICAL_RECORD, testResourceId, testOutcome);
        AuditLog deleteLog = new AuditLog(user, ActionType.DELETE, ResourceType.USER_PROFILE, testResourceId, testOutcome);

        assertThat(createLog.getActionType()).isEqualTo(ActionType.CREATE);
        assertThat(readLog.getActionType()).isEqualTo(ActionType.READ);
        assertThat(updateLog.getActionType()).isEqualTo(ActionType.UPDATE);
        assertThat(deleteLog.getActionType()).isEqualTo(ActionType.DELETE);
    }

    @Test
    void testAuditLogResourceTypes() {
        // Test data variables
        String testExternalAuthId = "ext-auth-audit4";
        String testFirstName = "Audit";
        String testLastName = "User4";
        String testEmail = "audit4@example.com";
        String testPhone = "+1234567890";
        LocalDate testDateOfBirth = LocalDate.of(1990, 1, 1);
        Gender testGender = Gender.OTHER;
        UserRole testRole = UserRole.PATIENT;

        UUID testResourceId = UUID.randomUUID();
        ActionType testActionType = ActionType.CREATE;
        Outcome testOutcome = Outcome.SUCCESS;

        User user = new User(testExternalAuthId, testFirstName, testLastName, testEmail,
                           testPhone, testDateOfBirth, testGender, testRole);

        // Test different resource types
        AuditLog userLog = new AuditLog(user, testActionType, ResourceType.USER_PROFILE, testResourceId, testOutcome);
        AuditLog patientLog = new AuditLog(user, testActionType, ResourceType.PATIENT_PROFILE, testResourceId, testOutcome);
        AuditLog providerLog = new AuditLog(user, testActionType, ResourceType.PROVIDER_PROFILE, testResourceId, testOutcome);
        AuditLog appointmentLog = new AuditLog(user, testActionType, ResourceType.APPOINTMENT, testResourceId, testOutcome);
        AuditLog medicalRecordLog = new AuditLog(user, testActionType, ResourceType.MEDICAL_RECORD, testResourceId, testOutcome);

        assertThat(userLog.getResourceType()).isEqualTo(ResourceType.USER_PROFILE);
        assertThat(patientLog.getResourceType()).isEqualTo(ResourceType.PATIENT_PROFILE);
        assertThat(providerLog.getResourceType()).isEqualTo(ResourceType.PROVIDER_PROFILE);
        assertThat(appointmentLog.getResourceType()).isEqualTo(ResourceType.APPOINTMENT);
        assertThat(medicalRecordLog.getResourceType()).isEqualTo(ResourceType.MEDICAL_RECORD);
    }
}
