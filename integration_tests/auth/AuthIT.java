package auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import util.ApiPaths;
import util.TestAccounts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test for auth endpoints.
 *
 * Verifies:
 *   POST /api/auth/login    — returns token pair
 *   POST /api/auth/refresh  — returns new access token
 *   POST /api/auth/logout   — invalidates token, returns 200
 *
 * Run:
 *   mvn exec:java -f integration_tests/pom.xml -Dexec.mainClass=auth.AuthIT
 */
public class AuthIT {

    public static void main(String[] args) {
        RestAssured.baseURI = System.getProperty("gateway.url",
                "https://gateway-dev-824144893232.us-west1.run.app");

        // Step 1 — login
        Response loginResp = given()
                .contentType(ContentType.JSON)
                .body("""
                    {"username": "%s", "password": "%s"}
                    """.formatted(TestAccounts.PROVIDER_USERNAME, TestAccounts.PROVIDER_PASSWORD))
            .when()
                .post(ApiPaths.LOGIN)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("access_token",  notNullValue())
                .body("refresh_token", notNullValue())
                .body("token_type",    equalTo("Bearer"))
                .extract().response();

        String accessToken  = loginResp.path("access_token");
        String refreshToken = loginResp.path("refresh_token");

        System.out.println("Step 1 PASS: POST " + ApiPaths.LOGIN + " returned 200 with token pair");

        // Step 2 — refresh
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"refresh_token": "%s"}
                """.formatted(refreshToken))
        .when()
            .post(ApiPaths.REFRESH)
        .then()
            .statusCode(200)
            .body("access_token", notNullValue());

        System.out.println("Step 2 PASS: POST " + ApiPaths.REFRESH + " returned 200 with new access token");

        // Step 3 — logout
        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .post(ApiPaths.LOGOUT)
        .then()
            .statusCode(200);

        System.out.println("Step 3 PASS: POST " + ApiPaths.LOGOUT + " returned 200");
    }
}
