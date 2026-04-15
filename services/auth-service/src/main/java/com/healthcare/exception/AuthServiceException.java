package com.healthcare.exception;

import org.springframework.http.HttpStatus;

/**
 * Auth-service domain exception.
 *
 * Carries an HTTP status and an internal error code for logging/audit.
 * The error code is NEVER sent to the client — only the HTTP status and
 * a generic message are returned. Full detail goes to logs and audit_logs.
 *
 * Usage:
 *   throw new AuthServiceException(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED",
 *       "JWT expired at 2026-03-31T10:15:00Z for user abc-123");
 *
 * Client receives:  401 Unauthorized
 * Logs receive:     TOKEN_EXPIRED — JWT expired at 2026-03-31T10:15:00Z for user abc-123
 */
public class AuthServiceException extends RuntimeException {

    // Internal error codes — for logging and audit_logs only
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ACCOUNT_INACTIVE    = "ACCOUNT_INACTIVE";
    public static final String INVALID_TOKEN       = "INVALID_TOKEN";
    public static final String TOKEN_EXPIRED       = "TOKEN_EXPIRED";
    public static final String TOKEN_BLACKLISTED   = "TOKEN_BLACKLISTED";
    public static final String SESSION_EXPIRED     = "SESSION_EXPIRED";
    public static final String INTERNAL_ERROR = "AUTH_INTERNAL_ERROR";

    private final HttpStatus status;
    private final String errorCode;

    public AuthServiceException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public AuthServiceException(HttpStatus status, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}