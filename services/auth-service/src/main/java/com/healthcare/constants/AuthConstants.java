package com.healthcare.constants;

/**
 * Shared constants for auth-service.
 *
 * Only values reused across multiple unrelated classes belong here.
 * Class-specific constants are defined as private static final in their own class.
 */
public final class AuthConstants {

    private AuthConstants() {}

    /**
     * Username format — letters, numbers, underscores only.
     * Reused in RegisterPatientRequest and RegisterProviderRequest.
     */
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";

    /**
     * Password must contain at least one uppercase letter, one lowercase letter,
     * one digit, and one special character (@$!%*?&^#).
     * Reused in RegisterPatientRequest and RegisterProviderRequest.
     */
    public static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&^#])[A-Za-z\\d@$!%*?&^#]+$";
}