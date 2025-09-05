package com.healthcare.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AppointmentStatus enum
 */
class AppointmentStatusTest {

    @Test
    void testAppointmentStatusValues() {
        assertThat(AppointmentStatus.values()).hasSize(7);
        assertThat(AppointmentStatus.AVAILABLE).isNotNull();
        assertThat(AppointmentStatus.SCHEDULED).isNotNull();
        assertThat(AppointmentStatus.CONFIRMED).isNotNull();
        assertThat(AppointmentStatus.IN_PROGRESS).isNotNull();
        assertThat(AppointmentStatus.COMPLETED).isNotNull();
        assertThat(AppointmentStatus.CANCELLED).isNotNull();
        assertThat(AppointmentStatus.NO_SHOW).isNotNull();
    }

    @Test
    void testAppointmentStatusCodes() {
        assertThat(AppointmentStatus.AVAILABLE.getCode()).isEqualTo("AVAILABLE");
        assertThat(AppointmentStatus.SCHEDULED.getCode()).isEqualTo("SCHEDULED");
        assertThat(AppointmentStatus.CONFIRMED.getCode()).isEqualTo("CONFIRMED");
        assertThat(AppointmentStatus.IN_PROGRESS.getCode()).isEqualTo("IN_PROGRESS");
        assertThat(AppointmentStatus.COMPLETED.getCode()).isEqualTo("COMPLETED");
        assertThat(AppointmentStatus.CANCELLED.getCode()).isEqualTo("CANCELLED");
        assertThat(AppointmentStatus.NO_SHOW.getCode()).isEqualTo("NO_SHOW");
    }

    @Test
    void testAppointmentStatusDescriptions() {
        assertThat(AppointmentStatus.AVAILABLE.getDescription()).isEqualTo("Appointment slot available");
        assertThat(AppointmentStatus.SCHEDULED.getDescription()).isEqualTo("Appointment scheduled");
        assertThat(AppointmentStatus.CONFIRMED.getDescription()).isEqualTo("Appointment confirmed");
        assertThat(AppointmentStatus.IN_PROGRESS.getDescription()).isEqualTo("Appointment in progress");
        assertThat(AppointmentStatus.COMPLETED.getDescription()).isEqualTo("Appointment completed");
        assertThat(AppointmentStatus.CANCELLED.getDescription()).isEqualTo("Appointment cancelled");
        assertThat(AppointmentStatus.NO_SHOW.getDescription()).isEqualTo("Patient did not show");
    }

    @Test
    void testAppointmentStatusValueOf() {
        assertThat(AppointmentStatus.valueOf("AVAILABLE")).isEqualTo(AppointmentStatus.AVAILABLE);
        assertThat(AppointmentStatus.valueOf("SCHEDULED")).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(AppointmentStatus.valueOf("CONFIRMED")).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(AppointmentStatus.valueOf("IN_PROGRESS")).isEqualTo(AppointmentStatus.IN_PROGRESS);
        assertThat(AppointmentStatus.valueOf("COMPLETED")).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(AppointmentStatus.valueOf("CANCELLED")).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(AppointmentStatus.valueOf("NO_SHOW")).isEqualTo(AppointmentStatus.NO_SHOW);
    }
}
