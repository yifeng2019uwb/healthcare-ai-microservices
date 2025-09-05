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
        assertThat(Gender.MALE).isNotNull();
        assertThat(Gender.FEMALE).isNotNull();
        assertThat(Gender.OTHER).isNotNull();
        assertThat(Gender.UNKNOWN).isNotNull();
    }

    @Test
    void testGenderCodes() {
        assertThat(Gender.MALE.getCode()).isEqualTo("MALE");
        assertThat(Gender.FEMALE.getCode()).isEqualTo("FEMALE");
        assertThat(Gender.OTHER.getCode()).isEqualTo("OTHER");
        assertThat(Gender.UNKNOWN.getCode()).isEqualTo("UNKNOWN");
    }

    @Test
    void testGenderDescriptions() {
        assertThat(Gender.MALE.getDescription()).isEqualTo("Male");
        assertThat(Gender.FEMALE.getDescription()).isEqualTo("Female");
        assertThat(Gender.OTHER.getDescription()).isEqualTo("Other");
        assertThat(Gender.UNKNOWN.getDescription()).isEqualTo("Unknown");
    }

    @Test
    void testGenderValueOf() {
        assertThat(Gender.valueOf("MALE")).isEqualTo(Gender.MALE);
        assertThat(Gender.valueOf("FEMALE")).isEqualTo(Gender.FEMALE);
        assertThat(Gender.valueOf("OTHER")).isEqualTo(Gender.OTHER);
        assertThat(Gender.valueOf("UNKNOWN")).isEqualTo(Gender.UNKNOWN);
    }
}
