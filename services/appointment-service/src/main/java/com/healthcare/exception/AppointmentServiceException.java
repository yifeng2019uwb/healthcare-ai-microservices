package com.healthcare.exception;

import org.springframework.http.HttpStatus;

public class AppointmentServiceException extends RuntimeException {

    public static final String PATIENT_NOT_FOUND   = "PATIENT_NOT_FOUND";
    public static final String PROVIDER_NOT_FOUND  = "PROVIDER_NOT_FOUND";
    public static final String ENCOUNTER_NOT_FOUND = "ENCOUNTER_NOT_FOUND";
    public static final String ACCESS_DENIED       = "APPOINTMENT_ACCESS_DENIED";

    private final HttpStatus status;
    private final String errorCode;

    public AppointmentServiceException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus()  { return status; }
    public String getErrorCode()   { return errorCode; }
}
