package com.healthcare.exception;

/**
 * Base exception class for all healthcare microservices exceptions.
 *
 * This abstract class provides a common foundation for all healthcare-specific
 * exceptions, ensuring consistent error handling across all services.
 *
 * Future Enhancements:
 * - Add HIPAA-specific error codes for compliance violations
 * - Add consent management error codes for patient privacy
 * - Add medical device integration error codes
 * - Add audit logging integration for compliance tracking
 *
 * @author Healthcare AI Microservices Team
 * @version 1.0
 * @since 2025-01-09
 */
public abstract class HealthcareException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;
    private final Object[] messageArgs;

    /**
     * Constructs a new healthcare exception with the specified error code and message.
     *
     * @param errorCode the error code for this exception
     * @param errorMessage the error message for this exception
     */
    protected HealthcareException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageArgs = null;
    }


    /**
     * Constructs a new healthcare exception with the specified error code, message, and cause.
     *
     * @param errorCode the error code for this exception
     * @param errorMessage the error message for this exception
     * @param cause the cause of this exception
     */
    protected HealthcareException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageArgs = null;
    }

    /**
     * Constructs a new healthcare exception with the specified error code, message, and message arguments.
     *
     * @param errorCode the error code for this exception
     * @param errorMessage the error message template for this exception
     * @param messageArgs the arguments to format the error message
     */
    protected HealthcareException(String errorCode, String errorMessage, Object... messageArgs) {
        super(formatMessage(errorMessage, messageArgs));
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageArgs = messageArgs;
    }

    /**
     * Constructs a new healthcare exception with the specified error code, message, cause, and message arguments.
     *
     * @param errorCode the error code for this exception
     * @param errorMessage the error message template for this exception
     * @param cause the cause of this exception
     * @param messageArgs the arguments to format the error message
     */
    protected HealthcareException(String errorCode, String errorMessage, Throwable cause, Object... messageArgs) {
        super(formatMessage(errorMessage, messageArgs), cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.messageArgs = messageArgs;
    }

    /**
     * Returns the error code for this exception.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the error message for this exception.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns the message arguments for this exception.
     *
     * @return the message arguments, or null if none
     */
    public Object[] getMessageArgs() {
        return messageArgs;
    }

    /**
     * Returns the formatted error message with arguments applied.
     *
     * @return the formatted error message
     */
    public String getFormattedMessage() {
        return getMessage();
    }

    /**
     * Formats a message template with the provided arguments.
     * Simple placeholder replacement using {} as placeholders.
     *
     * @param message the message template
     * @param args the arguments to substitute
     * @return the formatted message
     */
    private static String formatMessage(String message, Object... args) {
        if (args == null || args.length == 0) {
            return message;
        }

        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", String.valueOf(arg));
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s[errorCode=%s, message=%s]",
            getClass().getSimpleName(), errorCode, getFormattedMessage());
    }
}
