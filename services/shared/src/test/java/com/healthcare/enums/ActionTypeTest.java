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
    void testActionTypeValueOf() {
        assertThat(ActionType.valueOf("CREATE")).isEqualTo(ActionType.CREATE);
        assertThat(ActionType.valueOf("READ")).isEqualTo(ActionType.READ);
        assertThat(ActionType.valueOf("UPDATE")).isEqualTo(ActionType.UPDATE);
        assertThat(ActionType.valueOf("DELETE")).isEqualTo(ActionType.DELETE);
        assertThat(ActionType.valueOf("LOGIN")).isEqualTo(ActionType.LOGIN);
        assertThat(ActionType.valueOf("LOGOUT")).isEqualTo(ActionType.LOGOUT);
    }
}
