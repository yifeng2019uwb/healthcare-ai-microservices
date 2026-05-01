package provider;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import util.ApiPaths;
import util.LoginHelper;

import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for provider-service profile endpoints.
 *
 * Verifies:
 *   GET /api/provider/me                        — provider views own profile
 *   GET /api/provider/patients                  — provider views patient list
 *   GET /api/provider/patients/{id}             — provider views patient detail
 *   GET /api/provider/patients/{id}/conditions  — provider views patient conditions
 *   GET /api/provider/patients/{id}/allergies   — provider views patient allergies
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=provider.ProviderProfileIT
 */
public class ProviderProfileIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        // Step 1 — GET /api/provider/me
        LoginHelper.asProvider()
        .when()
            .get(ApiPaths.PROVIDER_ME)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",            notNullValue())
            .body("provider_code", notNullValue())
            .body("name",          notNullValue());

        System.out.println("Step 1 PASS: GET " + ApiPaths.PROVIDER_ME + " returned 200");

        // Step 2 — GET /api/provider/patients
        String patientId =
            LoginHelper.asProvider()
            .when()
                .get(ApiPaths.PROVIDER_PATIENTS)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().path("[0].id");

        System.out.println("Step 2 PASS: GET " + ApiPaths.PROVIDER_PATIENTS + " returned 200");

        if (patientId == null) {
            System.out.println("Steps 3-5 SKIP: no patients in provider list");
            return;
        }

        // Step 3 — GET /api/provider/patients/{id}
        LoginHelper.asProvider()
        .when()
            .get(ApiPaths.PROVIDER_PATIENTS + "/" + patientId)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",  notNullValue())
            .body("mrn", notNullValue());

        System.out.println("Step 3 PASS: GET " + ApiPaths.PROVIDER_PATIENTS + "/{id} returned 200");

        // Step 4 — GET /api/provider/patients/{id}/conditions
        LoginHelper.asProvider()
        .when()
            .get(ApiPaths.PROVIDER_PATIENTS + "/" + patientId + "/conditions")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        System.out.println("Step 4 PASS: GET " + ApiPaths.PROVIDER_PATIENTS + "/{id}/conditions returned 200");

        // Step 5 — GET /api/provider/patients/{id}/allergies
        LoginHelper.asProvider()
        .when()
            .get(ApiPaths.PROVIDER_PATIENTS + "/" + patientId + "/allergies")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON);

        System.out.println("Step 5 PASS: GET " + ApiPaths.PROVIDER_PATIENTS + "/{id}/allergies returned 200");
    }
}
