package encounter;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import util.ApiPaths;
import util.LoginHelper;

import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for patient encounter endpoints (Phase 1).
 *
 * Verifies:
 *   GET /api/encounters/me              — patient views own encounter list
 *   GET /api/encounters/me/{id}         — patient views encounter detail
 *
 * Prerequisites:
 *   - Seed accounts exist: run util.SeedAccounts first
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=encounter.PatientEncountersIT
 */
public class PatientEncountersIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        // Step 1 — login as patient
        String encounterId =
            LoginHelper.asPatient()
            .when()
                .get(ApiPaths.ENCOUNTERS_ME)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("total",      notNullValue())
                .body("page",       notNullValue())
                .body("encounters", notNullValue())
                .extract().path("encounters[0].id");

        System.out.println("Step 1 PASS: GET " + ApiPaths.ENCOUNTERS_ME + " returned 200");

        if (encounterId == null) {
            System.out.println("Step 2 SKIP: no encounters in DB");
            return;
        }

        // Step 2 — GET /api/encounters/me/{id}
        LoginHelper.asPatient()
        .when()
            .get(ApiPaths.ENCOUNTERS_ME + "/" + encounterId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",              notNullValue())
            .body("encounter_class", notNullValue());

        System.out.println("Step 2 PASS: GET " + ApiPaths.ENCOUNTERS_ME_DETAIL + " returned 200");

        // Step 3 — verify old path is no longer valid
        int oldPathStatus =
            LoginHelper.asPatient()
            .when()
                .get(ApiPaths.OLD_PATIENT_ENCOUNTERS)
            .then()
                .extract().statusCode();

        if (oldPathStatus == 404 || oldPathStatus == 500) {
            System.out.println("Step 3 PASS: old path " + ApiPaths.OLD_PATIENT_ENCOUNTERS + " returns " + oldPathStatus);
        } else {
            throw new AssertionError("Step 3 FAIL: old path should not return " + oldPathStatus);
        }
    }
}
