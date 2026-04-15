package util;

/**
 * Shared test account credentials.
 *
 * These accounts must exist in the DB before running integration tests.
 * Override any value with a system property, e.g.:
 *   -Dtest.provider.username=other_provider
 *
 * Seed script: integration_tests/util/SeedAccounts.java
 */
public final class TestAccounts {

    private TestAccounts() {}

    // ── Provider ─────────────────────────────────────────────────────────────
    public static final String PROVIDER_USERNAME =
            System.getProperty("test.provider.username", "drDeckow");
    public static final String PROVIDER_PASSWORD =
            System.getProperty("test.provider.password", "Password1@");
    public static final String PROVIDER_EMAIL =
            System.getProperty("test.provider.email", "smith@hospital.com");
    public static final String PROVIDER_CODE =
            System.getProperty("test.provider.code", "PRV-000001");
    public static final String PROVIDER_FIRST_NAME = "Louann705";
    public static final String PROVIDER_LAST_NAME  = "Deckow585";

    // ── Patient ───────────────────────────────────────────────────────────────
    public static final String PATIENT_USERNAME =
            System.getProperty("test.patient.username", "testpatient01");
    public static final String PATIENT_PASSWORD =
            System.getProperty("test.patient.password", "Password1@");
    public static final String PATIENT_EMAIL =
            System.getProperty("test.patient.email", "test01@example.com");
    public static final String PATIENT_MRN =
            System.getProperty("test.patient.mrn", "MRN-000002");
    public static final String PATIENT_FIRST_NAME = "Carly657";
    public static final String PATIENT_LAST_NAME  = "Pollich983";
}
