package auth;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.ApiPaths;
import util.BaseIT;
import util.TestAccounts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Auth Endpoints")
class AuthIT extends BaseIT {

    private static final String USERNAME        = TestAccounts.PROVIDER_USERNAME;
    private static final String PASSWORD        = TestAccounts.PROVIDER_PASSWORD;
    private static final String WRONG_PASSWORD  = "WrongPassword99!";
    private static final String INVALID_TOKEN   = "invalid.token.value";
    private static final String BEARER_PREFIX   = "Bearer ";

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void login_withValidCredentials_returns200WithTokenPair() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "%s", "password": "%s"}
                """.formatted(USERNAME, PASSWORD))
        .when()
            .post(ApiPaths.LOGIN)
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("access_token",  notNullValue())
            .body("refresh_token", notNullValue())
            .body("token_type",    equalTo("Bearer"));
    }

    @Test
    void login_withWrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "%s", "password": "%s"}
                """.formatted(USERNAME, WRONG_PASSWORD))
        .when()
            .post(ApiPaths.LOGIN)
        .then()
            .statusCode(401);
    }

    @Test
    void login_withMissingUsername_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"password": "%s"}
                """.formatted(PASSWORD))
        .when()
            .post(ApiPaths.LOGIN)
        .then()
            .statusCode(400);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Test
    @Disabled("TD-1: POST /refresh returns 503 — fix auth-service then re-enable")
    void refresh_withValidToken_returns200WithNewAccessToken() {
        Response loginResp = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "%s", "password": "%s"}
                """.formatted(USERNAME, PASSWORD))
        .when()
            .post(ApiPaths.LOGIN)
        .then()
            .extract().response();

        String refreshToken = loginResp.path("refresh_token");

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
    }

    @Test
    void refresh_withInvalidToken_returns4xx() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"refresh_token": "%s"}
                """.formatted(INVALID_TOKEN))
        .when()
            .post(ApiPaths.REFRESH)
        .then()
            .statusCode(greaterThanOrEqualTo(400));
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Test
    @Disabled("TD-2: POST /logout returns 503 — fix auth-service then re-enable")
    void logout_withValidToken_returns200() {
        String accessToken = given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "%s", "password": "%s"}
                """.formatted(USERNAME, PASSWORD))
        .when()
            .post(ApiPaths.LOGIN)
        .then()
            .extract().path("access_token");

        given()
            .header("Authorization", BEARER_PREFIX + accessToken)
        .when()
            .post(ApiPaths.LOGOUT)
        .then()
            .statusCode(200);
    }

    @Test
    void logout_withoutToken_returns401() {
        given()
        .when()
            .post(ApiPaths.LOGOUT)
        .then()
            .statusCode(401);
    }
}
