package util;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Shared login and auth header utility for integration tests.
 * RestAssured.baseURI must be set before calling these methods.
 */
public final class LoginHelper {

    public static final String AUTH_HEADER = "Authorization";

    private LoginHelper() {}

    /** Login as default provider, return access token. */
    public static String providerToken() {
        return login(TestAccounts.PROVIDER_USERNAME, TestAccounts.PROVIDER_PASSWORD);
    }

    /** Login as default patient, return access token. */
    public static String patientToken() {
        return login(TestAccounts.PATIENT_USERNAME, TestAccounts.PATIENT_PASSWORD);
    }

    /** Login with given credentials, return access token. */
    public static String login(String username, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "username": "%s",
                        "password": "%s"
                    }
                    """.formatted(username, password))
            .when()
                .post(ApiPaths.LOGIN)
            .then()
                .statusCode(200)
                .extract().path("access_token");
    }

    /** Returns a RequestSpecification with Bearer token header set. */
    public static RequestSpecification withToken(String token) {
        return given().header(AUTH_HEADER, "Bearer " + token);
    }

    /** Shortcut: login as provider and return spec with Bearer header. */
    public static RequestSpecification asProvider() {
        return withToken(providerToken());
    }

    /** Shortcut: login as patient and return spec with Bearer header. */
    public static RequestSpecification asPatient() {
        return withToken(patientToken());
    }
}
