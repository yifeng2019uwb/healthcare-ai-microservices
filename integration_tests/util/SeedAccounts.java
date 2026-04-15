package util;

import io.restassured.RestAssured;

/**
 * Verifies test accounts are reachable before running integration tests.
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=util.SeedAccounts
 *
 * If accounts don't exist yet:
 *   1. Register provider: POST /api/auth/register/provider
 *   2. Provider onboards patient: POST /api/provider/patients/onboard
 *   3. Patient registers auth account: POST /api/auth/register/patient  (with MRN from step 2)
 */
public class SeedAccounts {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        System.out.println("Verifying test accounts...");

        try {
            LoginHelper.providerToken();
            System.out.println("OK: provider " + TestAccounts.PROVIDER_USERNAME);
        } catch (Exception e) {
            System.out.println("FAIL: provider login failed — " + e.getMessage());
        }

        try {
            LoginHelper.patientToken();
            System.out.println("OK: patient " + TestAccounts.PATIENT_USERNAME);
        } catch (Exception e) {
            System.out.println("FAIL: patient login failed — " + e.getMessage());
        }
    }
}
