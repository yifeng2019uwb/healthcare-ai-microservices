package patient;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import util.ApiPaths;
import util.LoginHelper;

import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for patient-service profile endpoints.
 *
 * Verifies:
 *   GET /api/patients/me              — patient views own profile
 *   GET /api/patients/me/encounters   — patient views encounter list
 *   GET /api/patients/me/conditions   — patient views condition list
 *   GET /api/patients/me/allergies    — patient views allergy list
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=patient.PatientProfileIT
 */
public class PatientProfileIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        // Step 1 — GET /api/patients/me
        LoginHelper.asPatient()
        .when()
            .get(ApiPaths.PATIENT_ME)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",         notNullValue())
            .body("mrn",        notNullValue())
            .body("first_name", notNullValue());

        System.out.println("Step 1 PASS: GET " + ApiPaths.PATIENT_ME + " returned 200");

        // Step 2 — GET /api/patients/me/encounters
        LoginHelper.asPatient()
        .when()
            .get(ApiPaths.PATIENT_ENCOUNTERS)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        System.out.println("Step 2 PASS: GET " + ApiPaths.PATIENT_ENCOUNTERS + " returned 200");

        // Step 3 — GET /api/patients/me/conditions
        LoginHelper.asPatient()
        .when()
            .get(ApiPaths.PATIENT_CONDITIONS)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        System.out.println("Step 3 PASS: GET " + ApiPaths.PATIENT_CONDITIONS + " returned 200");

        // Step 4 — GET /api/patients/me/allergies
        LoginHelper.asPatient()
        .when()
            .get(ApiPaths.PATIENT_ALLERGIES)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        System.out.println("Step 4 PASS: GET " + ApiPaths.PATIENT_ALLERGIES + " returned 200");
    }
}
