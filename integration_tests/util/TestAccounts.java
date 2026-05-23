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
            System.getProperty("test.provider.username", "drDouglass");
    public static final String PROVIDER_PASSWORD =
            System.getProperty("test.provider.password", "Password1@");
    public static final String PROVIDER_EMAIL =
            System.getProperty("test.provider.email", "Douglass930@hospital.com");
    public static final String PROVIDER_FULL_NAME = "Douglass930 Windler79";

    // ── Patient ───────────────────────────────────────────────────────────────
    public static final String PATIENT_USERNAME =
            System.getProperty("test.patient.username", "testpatient01");
    public static final String PATIENT_PASSWORD =
            System.getProperty("test.patient.password", "Password1@");
    public static final String PATIENT_EMAIL =
            System.getProperty("test.patient.email", "test01@example.com");
    public static final String PATIENT_FIRST_NAME = "Jena102";
    public static final String PATIENT_LAST_NAME  = "Gislason620";
    public static final String PATIENT_DOB        = "1974-04-17";

    // ── Unregistered Patient (for registration happy path test — run once per env) ─
    public static final String UNREG_PATIENT_FIRST = "Monnie762";
    public static final String UNREG_PATIENT_LAST  = "Haag279";
    public static final String UNREG_PATIENT_DOB   = "2020-05-28";

    // ── Admin ─────────────────────────────────────────────────────────────────
    public static final String ADMIN_USERNAME =
            System.getProperty("test.admin.username", "admin123");
    public static final String ADMIN_PASSWORD =
            System.getProperty("test.admin.password", "Password123!");

    // ── Patient 2 (cross-patient isolation tests) ─────────────────────────────
    public static final String PATIENT2_USERNAME =
            System.getProperty("test.patient2.username", "testpatient02");
    public static final String PATIENT2_PASSWORD =
            System.getProperty("test.patient2.password", "Password1@");
    public static final String PATIENT2_EMAIL =
            System.getProperty("test.patient2.email", "test02@example.com");
    public static final String PATIENT2_MRN =
            System.getProperty("test.patient2.mrn", "MRN-000003");
}
