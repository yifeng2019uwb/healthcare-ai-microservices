package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AppointmentType enum
 */
class EncounterTypeTest {

    @Test
    void testAppointmentTypeValues() {
        assertThat(EncounterType.values()).hasSize(4);
        assertThat(EncounterType.REGULAR_CONSULTATION).isNotNull();
        assertThat(EncounterType.FOLLOW_UP).isNotNull();
        assertThat(EncounterType.NEW_PATIENT_INTAKE).isNotNull();
        assertThat(EncounterType.PROCEDURE_CONSULTATION).isNotNull();
    }

    @Test
    void testAppointmentTypeValueOf() {
        assertThat(EncounterType.valueOf("REGULAR_CONSULTATION")).isEqualTo(EncounterType.REGULAR_CONSULTATION);
        assertThat(EncounterType.valueOf("FOLLOW_UP")).isEqualTo(EncounterType.FOLLOW_UP);
        assertThat(EncounterType.valueOf("NEW_PATIENT_INTAKE")).isEqualTo(EncounterType.NEW_PATIENT_INTAKE);
        assertThat(EncounterType.valueOf("PROCEDURE_CONSULTATION")).isEqualTo(EncounterType.PROCEDURE_CONSULTATION);
    }

    @Test
    void testAppointmentTypeDurationMinutes() {
        assertThat(EncounterType.REGULAR_CONSULTATION.getDurationMinutes()).isEqualTo(30);
        assertThat(EncounterType.FOLLOW_UP.getDurationMinutes()).isEqualTo(15);
        assertThat(EncounterType.NEW_PATIENT_INTAKE.getDurationMinutes()).isEqualTo(60);
        assertThat(EncounterType.PROCEDURE_CONSULTATION.getDurationMinutes()).isEqualTo(45);
    }
}
