package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AppointmentType enum
 */
class AppointmentTypeTest {

    @Test
    void testAppointmentTypeValues() {
        assertThat(AppointmentType.values()).hasSize(4);
        assertThat(AppointmentType.REGULAR_CONSULTATION).isNotNull();
        assertThat(AppointmentType.FOLLOW_UP).isNotNull();
        assertThat(AppointmentType.NEW_PATIENT_INTAKE).isNotNull();
        assertThat(AppointmentType.PROCEDURE_CONSULTATION).isNotNull();
    }

    @Test
    void testAppointmentTypeCodes() {
        assertThat(AppointmentType.REGULAR_CONSULTATION.getCode()).isEqualTo("REGULAR_CONSULTATION");
        assertThat(AppointmentType.FOLLOW_UP.getCode()).isEqualTo("FOLLOW_UP");
        assertThat(AppointmentType.NEW_PATIENT_INTAKE.getCode()).isEqualTo("NEW_PATIENT_INTAKE");
        assertThat(AppointmentType.PROCEDURE_CONSULTATION.getCode()).isEqualTo("PROCEDURE_CONSULTATION");
    }

    @Test
    void testAppointmentTypeDescriptions() {
        assertThat(AppointmentType.REGULAR_CONSULTATION.getDescription()).isEqualTo("Regular consultation (30 minutes)");
        assertThat(AppointmentType.FOLLOW_UP.getDescription()).isEqualTo("Follow-up appointment (15 minutes)");
        assertThat(AppointmentType.NEW_PATIENT_INTAKE.getDescription()).isEqualTo("New patient intake (60 minutes)");
        assertThat(AppointmentType.PROCEDURE_CONSULTATION.getDescription()).isEqualTo("Procedure consultation (45 minutes)");
    }

    @Test
    void testAppointmentTypeValueOf() {
        assertThat(AppointmentType.valueOf("REGULAR_CONSULTATION")).isEqualTo(AppointmentType.REGULAR_CONSULTATION);
        assertThat(AppointmentType.valueOf("FOLLOW_UP")).isEqualTo(AppointmentType.FOLLOW_UP);
        assertThat(AppointmentType.valueOf("NEW_PATIENT_INTAKE")).isEqualTo(AppointmentType.NEW_PATIENT_INTAKE);
        assertThat(AppointmentType.valueOf("PROCEDURE_CONSULTATION")).isEqualTo(AppointmentType.PROCEDURE_CONSULTATION);
    }

    @Test
    void testAppointmentTypeDurationMinutes() {
        assertThat(AppointmentType.REGULAR_CONSULTATION.getDurationMinutes()).isEqualTo(30);
        assertThat(AppointmentType.FOLLOW_UP.getDurationMinutes()).isEqualTo(15);
        assertThat(AppointmentType.NEW_PATIENT_INTAKE.getDurationMinutes()).isEqualTo(60);
        assertThat(AppointmentType.PROCEDURE_CONSULTATION.getDurationMinutes()).isEqualTo(45);
    }
}
