package com.healthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * Patient-service domain exception.
 *
 * Carries an HTTP status and an internal error code for logging/audit.
 * The error code is NEVER sent to the client — only the HTTP status and
 * a generic message are returned.
 */
public class PatientServiceException extends RuntimeException {

    public static final String PATIENT_NOT_FOUND  = "PATIENT_NOT_FOUND";
    public static final String INTERNAL_ERROR     = "PATIENT_INTERNAL_ERROR";
    public static final String FORBIDDEN          = "PATIENT_FORBIDDEN";

    private final HttpStatus status;
    private final String errorCode;

    public PatientServiceException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus()    { return status; }
    public String getErrorCode()     { return errorCode; }
}
