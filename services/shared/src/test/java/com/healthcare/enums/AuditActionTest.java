package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuditAction enum
 */
class AuditActionTest {

    @Test
    void testAuditActionValues() {
        assertThat(AuditAction.values()).hasSize(6);
        assertThat(AuditAction.CREATE).isNotNull();
        assertThat(AuditAction.READ).isNotNull();
        assertThat(AuditAction.UPDATE).isNotNull();
        assertThat(AuditAction.DELETE).isNotNull();
        assertThat(AuditAction.LOGIN).isNotNull();
        assertThat(AuditAction.LOGOUT).isNotNull();
    }

    @Test
    void testAuditActionCodes() {
        assertThat(AuditAction.CREATE.getCode()).isEqualTo("CREATE");
        assertThat(AuditAction.READ.getCode()).isEqualTo("READ");
        assertThat(AuditAction.UPDATE.getCode()).isEqualTo("UPDATE");
        assertThat(AuditAction.DELETE.getCode()).isEqualTo("DELETE");
        assertThat(AuditAction.LOGIN.getCode()).isEqualTo("LOGIN");
        assertThat(AuditAction.LOGOUT.getCode()).isEqualTo("LOGOUT");
    }

    @Test
    void testAuditActionDescriptions() {
        assertThat(AuditAction.CREATE.getDescription()).isEqualTo("Record created");
        assertThat(AuditAction.READ.getDescription()).isEqualTo("Record accessed");
        assertThat(AuditAction.UPDATE.getDescription()).isEqualTo("Record updated");
        assertThat(AuditAction.DELETE.getDescription()).isEqualTo("Record deleted");
        assertThat(AuditAction.LOGIN.getDescription()).isEqualTo("User login");
        assertThat(AuditAction.LOGOUT.getDescription()).isEqualTo("User logout");
    }

    @Test
    void testAuditActionValueOf() {
        assertThat(AuditAction.valueOf("CREATE")).isEqualTo(AuditAction.CREATE);
        assertThat(AuditAction.valueOf("READ")).isEqualTo(AuditAction.READ);
        assertThat(AuditAction.valueOf("UPDATE")).isEqualTo(AuditAction.UPDATE);
        assertThat(AuditAction.valueOf("DELETE")).isEqualTo(AuditAction.DELETE);
        assertThat(AuditAction.valueOf("LOGIN")).isEqualTo(AuditAction.LOGIN);
        assertThat(AuditAction.valueOf("LOGOUT")).isEqualTo(AuditAction.LOGOUT);
    }
}
