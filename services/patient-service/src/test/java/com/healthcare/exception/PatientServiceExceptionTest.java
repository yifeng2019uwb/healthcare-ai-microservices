package com.healthcare.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class PatientServiceExceptionTest {

    @Test
    void constructor_setsAllFields() {
        PatientServiceException ex = new PatientServiceException(
                HttpStatus.NOT_FOUND, PatientServiceException.PATIENT_NOT_FOUND, "Patient not found");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getErrorCode()).isEqualTo(PatientServiceException.PATIENT_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Patient not found");
    }

    @Test
    void errorCodeConstants_areDefinied() {
        assertThat(PatientServiceException.PATIENT_NOT_FOUND).isEqualTo("PATIENT_NOT_FOUND");
        assertThat(PatientServiceException.INTERNAL_ERROR).isEqualTo("PATIENT_INTERNAL_ERROR");
        assertThat(PatientServiceException.FORBIDDEN).isEqualTo("PATIENT_FORBIDDEN");
    }

    @Test
    void exception_isThrownAndCaught() {
        try {
            throw new PatientServiceException(HttpStatus.FORBIDDEN,
                    PatientServiceException.FORBIDDEN, "Access denied");
        } catch (PatientServiceException e) {
            assertThat(e.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(e.getErrorCode()).isEqualTo("PATIENT_FORBIDDEN");
        }
    }
}
