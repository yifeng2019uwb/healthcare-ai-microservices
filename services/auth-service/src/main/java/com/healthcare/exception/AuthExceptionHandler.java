package com.healthcare.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for auth-service.
 *
 * Principles:
 * - Client always receives a generic, safe message — no internal details leaked
 * - Internal error codes and full messages go to logs only
 * - Validation errors are safe to expose — they describe what the client sent wrong
 * - AuthServiceException carries its own HTTP status — handler just reads it
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    // -------------------------------------------------------------------------
    // Auth domain errors — 401 / 403
    // -------------------------------------------------------------------------

    @ExceptionHandler(AuthServiceException.class)
    public ResponseEntity<ErrorResponse> handleAuthServiceException(AuthServiceException e) {
        // Full detail logged internally — error code tells us exactly what happened
        log.warn("Auth error [{}]: {}", e.getErrorCode(), e.getMessage());
        // Client gets only HTTP status + generic reason phrase
        return ResponseEntity
                .status(e.getStatus())
                .body(ErrorResponse.of(e.getStatus().value(), e.getStatus().getReasonPhrase()));
    }

    // -------------------------------------------------------------------------
    // Validation errors — 400 (safe to expose, client caused these)
    // -------------------------------------------------------------------------

    /**
     * Handles @Valid failures on @RequestBody — most common validation path.
     * Extracts the first field error message and returns it to the client.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        log.debug("Validation error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, message));
    }

    /**
     * Handles @Validated failures on method parameters.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("Validation failed");
        log.debug("Constraint violation: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, message));
    }

    /**
     * Handles malformed JSON request bodies.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        log.debug("Malformed request body: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Malformed request body"));
    }

    /**
     * Handles missing required headers — e.g. Authorization header on logout.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException e) {
        log.debug("Missing required header: {}", e.getHeaderName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(400, "Missing required header: " + e.getHeaderName()));
    }

    // -------------------------------------------------------------------------
    // Catch-all — 500 (nothing internal leaked)
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        // Full stack trace logged — never sent to client
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "An unexpected error occurred"));
    }
}