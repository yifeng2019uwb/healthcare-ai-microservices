package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Outcome enum
 */
class OutcomeTest {

    @Test
    void testOutcomeValues() {
        assertThat(Outcome.values()).hasSize(2);
        assertThat(Outcome.SUCCESS).isNotNull();
        assertThat(Outcome.FAILURE).isNotNull();
    }

    @Test
    void testOutcomeCodes() {
        assertThat(Outcome.SUCCESS.getCode()).isEqualTo("SUCCESS");
        assertThat(Outcome.FAILURE.getCode()).isEqualTo("FAILURE");
    }

    @Test
    void testOutcomeDescriptions() {
        assertThat(Outcome.SUCCESS.getDescription()).isEqualTo("Operation successful");
        assertThat(Outcome.FAILURE.getDescription()).isEqualTo("Operation failed");
    }

    @Test
    void testOutcomeValueOf() {
        assertThat(Outcome.valueOf("SUCCESS")).isEqualTo(Outcome.SUCCESS);
        assertThat(Outcome.valueOf("FAILURE")).isEqualTo(Outcome.FAILURE);
    }
}
