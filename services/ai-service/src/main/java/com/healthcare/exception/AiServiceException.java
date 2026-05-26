package com.healthcare.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends RuntimeException {

    public static final String PATIENT_NOT_FOUND      = "AI_PATIENT_NOT_FOUND";
    public static final String ENCOUNTER_NOT_FOUND    = "AI_ENCOUNTER_NOT_FOUND";
    public static final String NO_ANALYSIS_FOUND      = "AI_NO_ANALYSIS_FOUND";
    public static final String PROVIDER_NOT_AUTHORIZED = "AI_PROVIDER_NOT_AUTHORIZED";
    public static final String GEMINI_ERROR           = "AI_GEMINI_ERROR";
    public static final String INTERNAL_ERROR         = "AI_INTERNAL_ERROR";

    private final HttpStatus status;
    private final String errorCode;

    public AiServiceException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus()  { return status; }
    public String getErrorCode()   { return errorCode; }
}
