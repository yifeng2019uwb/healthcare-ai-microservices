package auth;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.ApiPaths;
import util.BaseIT;
import util.TestAccounts;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@DisplayName("Register Provider Endpoint")
class RegisterProviderIT extends BaseIT {

    // ── Request field names (API contract) ────────────────────────────────────
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_EMAIL    = "email";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_NAME     = "name";

    // ── Known registered provider (seed data) ────────────────────────────────
    private static final String VALID_USED_USERNAME = TestAccounts.PROVIDER_USERNAME;
    private static final String VALID_USED_EMAIL    = TestAccounts.PROVIDER_EMAIL;
    private static final String REGISTERED_NAME   = TestAccounts.PROVIDER_FULL_NAME;

    // ── Valid / invalid input constants ───────────────────────────────────────
    private static final String VALID_PASSWORD = "Password1@";
    private static final String WEAK_PASSWORD  = "Password123";   // missing special char
    private static final String INVALID_EMAIL  = "not-an-email";

    // ── Username / email format patterns ──────────────────────────────────────
    private static final String USERNAME_PATTERN = "prov_%s";
    private static final String EMAIL_PATTERN    = "prov_%s@example.com";

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @Disabled("Stateful: run once per environment, then disable (provider becomes registered). " +
              "Set -Dtest.provider.unreg.name=<full name from providers table>.")
    void register_withValidUnregisteredProvider_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, VALID_USED_USERNAME,
                FIELD_EMAIL,    VALID_USED_EMAIL,
                FIELD_PASSWORD, VALID_PASSWORD,
                FIELD_NAME,     REGISTERED_NAME))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(201);
    }

    // ── Validation — bean constraints (400) ───────────────────────────────────

    @Test
    void register_withMissingName_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,    EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD, VALID_PASSWORD))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(400);
    }

    @Test
    void register_withPasswordMissingSpecialCharacter_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,    EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD, WEAK_PASSWORD,
                FIELD_NAME,     "Nonexistent Provider"))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(400);
    }

    @Test
    void register_withInvalidEmailFormat_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,    INVALID_EMAIL,
                FIELD_PASSWORD, VALID_PASSWORD,
                FIELD_NAME,     "Nonexistent Provider"))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(400);
    }

    // ── Domain errors ─────────────────────────────────────────────────────────

    @Test
    void register_withUnknownProviderName_returns422() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,    EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD, VALID_PASSWORD,
                FIELD_NAME,     "Nonexistent Provider"))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(422);
    }

    @Test
    void register_withAlreadyRegisteredProvider_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,    EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD, VALID_PASSWORD,
                FIELD_NAME,     REGISTERED_NAME))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(409);
    }

    @Test
    void register_withAlreadyTakenUsername_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, VALID_USED_USERNAME,
                FIELD_EMAIL,    EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD, VALID_PASSWORD,
                FIELD_NAME,     "Nonexistent Provider"))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(409);
    }

    @Test
    void register_withAlreadyTakenEmail_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME, USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,    VALID_USED_EMAIL,
                FIELD_PASSWORD, VALID_PASSWORD,
                FIELD_NAME,     "Nonexistent Provider"))
        .when()
            .post(ApiPaths.REGISTER_PROVIDER)
        .then()
            .statusCode(409);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
