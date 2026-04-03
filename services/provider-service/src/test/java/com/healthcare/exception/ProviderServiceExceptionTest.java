package com.healthcare.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProviderServiceExceptionTest {

    @Test
    void constructor_storesStatusCodeAndMessage() {
        ProviderServiceException ex = new ProviderServiceException(
                HttpStatus.NOT_FOUND, ProviderServiceException.PROVIDER_NOT_FOUND, "Provider not found");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getErrorCode()).isEqualTo(ProviderServiceException.PROVIDER_NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Provider not found");
    }

    @Test
    void constants_haveExpectedValues() {
        assertThat(ProviderServiceException.PROVIDER_NOT_FOUND).isEqualTo("PROVIDER_NOT_FOUND");
        assertThat(ProviderServiceException.PATIENT_NOT_FOUND).isEqualTo("PATIENT_NOT_FOUND");
        assertThat(ProviderServiceException.ACCESS_DENIED).isEqualTo("PROVIDER_ACCESS_DENIED");
        assertThat(ProviderServiceException.INTERNAL_ERROR).isEqualTo("PROVIDER_INTERNAL_ERROR");
    }

    @Test
    void isRuntimeException_andCanBeCaught() {
        assertThatThrownBy(() -> {
            throw new ProviderServiceException(
                    HttpStatus.FORBIDDEN, ProviderServiceException.ACCESS_DENIED, "Forbidden");
        })
                .isInstanceOf(ProviderServiceException.class)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Forbidden");
    }
}
