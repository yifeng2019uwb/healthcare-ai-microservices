package encounter;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import util.ApiPaths;
import util.LoginHelper;

import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for provider encounter endpoints (Phase 1).
 *
 * Verifies:
 *   GET /api/encounters/provider                  — provider views own encounter list
 *   GET /api/encounters/provider/{id}             — provider views encounter detail
 *   GET /api/encounters/provider/patients/{id}    — provider views patient's encounters
 *
 * Prerequisites:
 *   - Seed accounts exist: run util.SeedAccounts first
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=encounter.ProviderEncountersIT
 */
public class ProviderEncountersIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        // Step 1 — GET /api/encounters/provider
        String encounterId =
            LoginHelper.asProvider()
            .when()
                .get(ApiPaths.ENCOUNTERS_PROVIDER)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("total",      notNullValue())
                .body("encounters", notNullValue())
                .extract().path("encounters[0].id");

        System.out.println("Step 1 PASS: GET " + ApiPaths.ENCOUNTERS_PROVIDER + " returned 200");

        if (encounterId == null) {
            System.out.println("Steps 2-4 SKIP: no encounters in DB");
            return;
        }

        // Step 2 — GET /api/encounters/provider/{id}
        String patientId =
            LoginHelper.asProvider()
            .when()
                .get(ApiPaths.ENCOUNTERS_PROVIDER + "/" + encounterId)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .extract().path("patient.id");

        System.out.println("Step 2 PASS: GET " + ApiPaths.ENCOUNTERS_PROVIDER_DETAIL + " returned 200");

        if (patientId == null) {
            System.out.println("Steps 3-4 SKIP: no patient id in encounter detail");
            return;
        }

        // Step 3 — GET /api/encounters/provider/patients/{patientId}
        LoginHelper.asProvider()
        .when()
            .get(ApiPaths.ENCOUNTERS_PROVIDER + "/patients/" + patientId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        System.out.println("Step 3 PASS: GET " + ApiPaths.ENCOUNTERS_PROVIDER_PATIENT + " returned 200");

        // Step 4 — verify old path is no longer valid
        int oldPathStatus =
            LoginHelper.asProvider()
            .when()
                .get(ApiPaths.OLD_PROVIDER_ENCOUNTERS)
            .then()
                .extract().statusCode();

        if (oldPathStatus == 404 || oldPathStatus == 500) {
            System.out.println("Step 4 PASS: old path " + ApiPaths.OLD_PROVIDER_ENCOUNTERS + " returns " + oldPathStatus);
        } else {
            throw new AssertionError("Step 4 FAIL: old path should not return " + oldPathStatus);
        }
    }
}
