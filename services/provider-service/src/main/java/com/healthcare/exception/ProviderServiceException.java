package com.healthcare.exception;

import org.springframework.http.HttpStatus;

public class ProviderServiceException extends RuntimeException {

    public static final String PROVIDER_NOT_FOUND  = "PROVIDER_NOT_FOUND";
    public static final String PATIENT_NOT_FOUND   = "PATIENT_NOT_FOUND";
    public static final String ACCESS_DENIED        = "PROVIDER_ACCESS_DENIED";
    public static final String INTERNAL_ERROR       = "PROVIDER_INTERNAL_ERROR";

    private final HttpStatus status;
    private final String errorCode;

    public ProviderServiceException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus()  { return status; }
    public String getErrorCode()   { return errorCode; }
}
