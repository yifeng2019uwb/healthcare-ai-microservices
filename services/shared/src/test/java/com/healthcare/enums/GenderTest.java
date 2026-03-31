package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Gender enum
 */
class GenderTest {

    @Test
    void testGenderValues() {
        assertThat(Gender.values()).hasSize(4);
        assertThat(Gender.M).isNotNull();
        assertThat(Gender.F).isNotNull();
        assertThat(Gender.O).isNotNull();
        assertThat(Gender.UNKNOWN).isNotNull();
    }

    @Test
    void testGenderValueOf() {
        assertThat(Gender.valueOf("M")).isEqualTo(Gender.M);
        assertThat(Gender.valueOf("F")).isEqualTo(Gender.F);
        assertThat(Gender.valueOf("O")).isEqualTo(Gender.O);
        assertThat(Gender.valueOf("UNKNOWN")).isEqualTo(Gender.UNKNOWN);
    }
}
