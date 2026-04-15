package auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for POST /api/auth/register/patient.
 *
 * Prerequisites:
 *   - Gateway running at gateway.url (default: https://gateway-dev-824144893232.us-west1.run.app)
 *   - auth-service running and reachable by the gateway
 *   - A patient record exists in the DB matching the mrn / firstName / lastName below
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml
 *
 * Override:
 *   mvn exec:java -f integration_tests/pom.xml \
 *     -Dgateway.url=https://gateway-dev-824144893232.us-west1.run.app \
 *     -Dtest.patient.mrn=MRN001 \
 *     -Dtest.patient.firstName=John \
 *     -Dtest.patient.lastName=Doe
 */
public class RegisterPatientIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url", "https://gateway-dev-824144893232.us-west1.run.app");

        String mrn       = System.getProperty("test.patient.mrn",       "MRN001");
        String firstName = System.getProperty("test.patient.firstName", "John");
        String lastName  = System.getProperty("test.patient.lastName",  "Doe");

        // Timestamp suffix prevents username/email conflicts on repeated runs
        String suffix   = String.valueOf(System.currentTimeMillis());
        String username = "test_user_" + suffix;
        String email    = "test_" + suffix + "@example.com";

        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "username":   "%s",
                    "email":      "%s",
                    "password":   "Password1@",
                    "mrn":        "%s",
                    "firstName":  "%s",
                    "lastName":   "%s"
                }
                """.formatted(username, email, mrn, firstName, lastName))
        .when()
            .post("/api/auth/register/patient")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("access_token",  notNullValue())
            .body("refresh_token", notNullValue())
            .body("token_type",    equalTo("Bearer"))
            .body("expires_in",    equalTo(900));

        System.out.println("PASS: registerPatient returned 201 with token pair");
    }
}
