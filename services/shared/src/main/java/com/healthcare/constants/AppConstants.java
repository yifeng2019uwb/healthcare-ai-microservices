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

    // Security constants (headers, JWT claims, roles) → SecurityConstants.java

    public static final String SUPPRESS_UNUSED = "unused";


    public static final String FIELD_PATIENT_ID   = "patientId";
    public static final String FIELD_ENCOUNTER_ID = "encounterId";

}