package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ActionType enum
 */
class ActionTypeTest {

    @Test
    void testActionTypeValues() {
        assertThat(ActionType.values()).hasSize(6);
        assertThat(ActionType.CREATE).isNotNull();
        assertThat(ActionType.READ).isNotNull();
        assertThat(ActionType.UPDATE).isNotNull();
        assertThat(ActionType.DELETE).isNotNull();
        assertThat(ActionType.LOGIN).isNotNull();
        assertThat(ActionType.LOGOUT).isNotNull();
    }

    @Test
    void testActionTypeCodes() {
        assertThat(ActionType.CREATE.getCode()).isEqualTo("CREATE");
        assertThat(ActionType.READ.getCode()).isEqualTo("READ");
        assertThat(ActionType.UPDATE.getCode()).isEqualTo("UPDATE");
        assertThat(ActionType.DELETE.getCode()).isEqualTo("DELETE");
        assertThat(ActionType.LOGIN.getCode()).isEqualTo("LOGIN");
        assertThat(ActionType.LOGOUT.getCode()).isEqualTo("LOGOUT");
    }

    @Test
    void testActionTypeDescriptions() {
        assertThat(ActionType.CREATE.getDescription()).isEqualTo("Create operation");
        assertThat(ActionType.READ.getDescription()).isEqualTo("Read operation");
        assertThat(ActionType.UPDATE.getDescription()).isEqualTo("Update operation");
        assertThat(ActionType.DELETE.getDescription()).isEqualTo("Delete operation");
        assertThat(ActionType.LOGIN.getDescription()).isEqualTo("User login");
        assertThat(ActionType.LOGOUT.getDescription()).isEqualTo("User logout");
    }

    @Test
    void testActionTypeValueOf() {
        assertThat(ActionType.valueOf("CREATE")).isEqualTo(ActionType.CREATE);
        assertThat(ActionType.valueOf("READ")).isEqualTo(ActionType.READ);
        assertThat(ActionType.valueOf("UPDATE")).isEqualTo(ActionType.UPDATE);
        assertThat(ActionType.valueOf("DELETE")).isEqualTo(ActionType.DELETE);
        assertThat(ActionType.valueOf("LOGIN")).isEqualTo(ActionType.LOGIN);
        assertThat(ActionType.valueOf("LOGOUT")).isEqualTo(ActionType.LOGOUT);
    }
}
