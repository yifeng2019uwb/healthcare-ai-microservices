package util;

/**
 * API path constants for all integration tests.
 */
public final class ApiPaths {

    private ApiPaths() {}

    // ── Auth ─────────────────────────────────────────────────────────────────
    public static final String LOGIN             = "/api/auth/login";
    public static final String REGISTER_PATIENT  = "/api/auth/register/patient";
    public static final String REGISTER_PROVIDER = "/api/auth/register/provider";
    public static final String REFRESH           = "/api/auth/refresh";
    public static final String LOGOUT            = "/api/auth/logout";

    // ── Provider ──────────────────────────────────────────────────────────────
    public static final String PROVIDER_ONBOARD  = "/api/provider/patients/onboard";
    public static final String PROVIDER_PATIENTS = "/api/provider/patients";
    public static final String PROVIDER_PROFILE  = "/api/provider/profile";

    // ── Patient ───────────────────────────────────────────────────────────────
    public static final String PATIENT_PROFILE   = "/api/patients/profile";

    // ── Encounters (patient) ──────────────────────────────────────────────────
    public static final String ENCOUNTERS_ME          = "/api/encounters/me";
    public static final String ENCOUNTERS_ME_DETAIL   = "/api/encounters/me/{id}";

    // ── Encounters (provider) ─────────────────────────────────────────────────
    public static final String ENCOUNTERS_PROVIDER         = "/api/encounters/provider";
    public static final String ENCOUNTERS_PROVIDER_DETAIL  = "/api/encounters/provider/{id}";
    public static final String ENCOUNTERS_PROVIDER_PATIENT = "/api/encounters/provider/patients/{patientId}";

    // ── Old paths (should return 4xx/5xx) ────────────────────────────────────
    public static final String OLD_PATIENT_ENCOUNTERS  = "/api/appointments/me/encounters";
    public static final String OLD_PROVIDER_ENCOUNTERS = "/api/appointments/provider/encounters";
}
