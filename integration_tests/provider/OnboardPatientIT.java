package provider;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import util.ApiPaths;
import util.LoginHelper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for POST /api/provider/patients/onboard.
 *
 * Prerequisites:
 *   - Seed accounts exist: run util.SeedAccounts first
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=provider.OnboardPatientIT
 */
public class OnboardPatientIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        String suffix = String.valueOf(System.currentTimeMillis());

        LoginHelper.asProvider()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "first_name": "Test",
                    "last_name":  "Patient%s",
                    "birthdate":  "1990-06-15",
                    "gender":     "M",
                    "phone":      "+12065550199"
                }
                """.formatted(suffix))
        .when()
            .post(ApiPaths.PROVIDER_ONBOARD)
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id",         notNullValue())
            .body("mrn",        matchesPattern("MRN-\\d+"))
            .body("first_name", equalTo("Test"))
            .body("last_name",  equalTo("Patient" + suffix));

        System.out.println("PASS: POST " + ApiPaths.PROVIDER_ONBOARD + " returned 201 with MRN");
    }
}
