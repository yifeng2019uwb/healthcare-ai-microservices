package com.healthcare.constants;

/**
 * System-wide application constants shared across all healthcare services.
 *
 * These are values that appear in multiple services and must stay consistent.
 * Do NOT put service-specific constants here — those belong in each service's
 * own constants file (e.g. PatientServiceConstants).
 */
public final class AppConstants {

    private AppConstants() {}

    // ------------------------------------------------------------------
    // Audit
    // ------------------------------------------------------------------

    /**
     * Used as the updatedBy value for automated/system operations
     * where no authenticated user context is available (e.g. registration).
     */
    public static final String SYSTEM_USER = "system";

    // ------------------------------------------------------------------
    // JWT / HTTP headers passed between gateway and downstream services
    // ------------------------------------------------------------------

    /** Internal header carrying the authenticated user's UUID. */
    public static final String HEADER_USER_ID   = "X-User-Id";

    /** Internal header carrying the authenticated user's role (PATIENT / PROVIDER). */
    public static final String HEADER_USER_ROLE = "X-User-Role";

    /** Internal header carrying the authenticated user's username. */
    public static final String HEADER_USERNAME  = "X-Username";

    // ------------------------------------------------------------------
    // JWT claims
    // ------------------------------------------------------------------

    public static final String JWT_CLAIM_ROLE     = "role";
    public static final String JWT_CLAIM_USERNAME = "username";
    public static final String JWT_CLAIM_EMAIL    = "email";


    // ------------------------------------------------------------------
    // User roles (string form — mirrors UserRole enum, for use in
    // Spring Security expressions like hasRole(...))
    // ------------------------------------------------------------------

    public static final String ROLE_PATIENT  = "PATIENT";
    public static final String ROLE_PROVIDER = "PROVIDER";

    public static final String SUPPRESS_UNUSED = "unused";


    public static final String FIELD_PATIENT_ID   = "patientId";
    public static final String FIELD_ENCOUNTER_ID = "encounterId";

}