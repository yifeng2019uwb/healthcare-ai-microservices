package com.healthcare.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentServiceExceptionTest {

    @Test
    void constructor_storesStatusCodeAndMessage() {
        AppointmentServiceException ex = new AppointmentServiceException(
                HttpStatus.NOT_FOUND, AppointmentServiceException.ENCOUNTER_NOT_FOUND, "Encounter not found");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getErrorCode()).isEqualTo(AppointmentServiceException.ENCOUNTER_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Encounter not found");
    }

    @Test
    void constants_haveExpectedValues() {
        assertThat(AppointmentServiceException.PATIENT_NOT_FOUND).isEqualTo("PATIENT_NOT_FOUND");
        assertThat(AppointmentServiceException.PROVIDER_NOT_FOUND).isEqualTo("PROVIDER_NOT_FOUND");
        assertThat(AppointmentServiceException.ENCOUNTER_NOT_FOUND).isEqualTo("ENCOUNTER_NOT_FOUND");
        assertThat(AppointmentServiceException.ACCESS_DENIED).isEqualTo("APPOINTMENT_ACCESS_DENIED");
    }

    @Test
    void isRuntimeException_andCanBeCaught() {
        assertThatThrownBy(() -> {
            throw new AppointmentServiceException(
                    HttpStatus.FORBIDDEN, AppointmentServiceException.ACCESS_DENIED, "Forbidden");
        })
                .isInstanceOf(AppointmentServiceException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Forbidden");
    }
}
