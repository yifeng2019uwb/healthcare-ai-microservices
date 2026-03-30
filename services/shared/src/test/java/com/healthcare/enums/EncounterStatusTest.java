package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EncounterStatus enum
 */
class EncounterStatusTest {

    @Test
    void testEncounterStatusValues() {
        assertThat(EncounterStatus.values()).hasSize(6);
        assertThat(EncounterStatus.AVAILABLE).isNotNull();
        assertThat(EncounterStatus.SCHEDULED).isNotNull();
        assertThat(EncounterStatus.IN_PROGRESS).isNotNull();
        assertThat(EncounterStatus.COMPLETED).isNotNull();
        assertThat(EncounterStatus.CANCELLED).isNotNull();
        assertThat(EncounterStatus.NO_SHOW).isNotNull();
    }

    @Test
    void testEncounterStatusValueOf() {
        assertThat(EncounterStatus.valueOf("AVAILABLE")).isEqualTo(EncounterStatus.AVAILABLE);
        assertThat(EncounterStatus.valueOf("SCHEDULED")).isEqualTo(EncounterStatus.SCHEDULED);
        assertThat(EncounterStatus.valueOf("IN_PROGRESS")).isEqualTo(EncounterStatus.IN_PROGRESS);
        assertThat(EncounterStatus.valueOf("COMPLETED")).isEqualTo(EncounterStatus.COMPLETED);
        assertThat(EncounterStatus.valueOf("CANCELLED")).isEqualTo(EncounterStatus.CANCELLED);
        assertThat(EncounterStatus.valueOf("NO_SHOW")).isEqualTo(EncounterStatus.NO_SHOW);
    }
}
