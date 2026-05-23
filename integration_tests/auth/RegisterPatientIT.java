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

@DisplayName("Register Patient Endpoint")
class RegisterPatientIT extends BaseIT {

    // ── Request field names (API contract) ────────────────────────────────────
    private static final String FIELD_USERNAME   = "username";
    private static final String FIELD_EMAIL      = "email";
    private static final String FIELD_PASSWORD   = "password";
    private static final String FIELD_FIRST_NAME = "first_name";
    private static final String FIELD_LAST_NAME  = "last_name";
    private static final String FIELD_BIRTHDATE  = "birthdate";

    // ── Known registered patient (seed data) ──────────────────────────────────
    private static final String VALID_USED_USERNAME = TestAccounts.PATIENT_USERNAME;
    private static final String VALID_USED_EMAIL    = TestAccounts.PATIENT_EMAIL;
    private static final String REGISTERED_FIRST  = TestAccounts.PATIENT_FIRST_NAME;
    private static final String REGISTERED_LAST   = TestAccounts.PATIENT_LAST_NAME;
    private static final String REGISTERED_DOB    = TestAccounts.PATIENT_DOB;

    // ── Unregistered patient — run once per env, then disable ─────────────────
    private static final String UNREGISTERED_FIRST = TestAccounts.UNREG_PATIENT_FIRST;
    private static final String UNREGISTERED_LAST  = TestAccounts.UNREG_PATIENT_LAST;
    private static final String UNREGISTERED_DOB   = TestAccounts.UNREG_PATIENT_DOB;

    // ── Valid / invalid input constants ───────────────────────────────────────
    private static final String VALID_PASSWORD = "Password1@";
    private static final String WEAK_PASSWORD  = "Password123";   // missing special char
    private static final String INVALID_EMAIL  = "not-an-email";

    // ── Username / email format patterns ──────────────────────────────────────
    private static final String USERNAME_PATTERN = "user_%s";
    private static final String EMAIL_PATTERN    = "e_%s@example.com";

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @Disabled("Stateful: run once per environment, then disable (patient becomes registered).")
    void register_withValidUnregisteredPatient_returns201() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   VALID_USED_USERNAME,
                FIELD_EMAIL,      VALID_USED_EMAIL,
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, REGISTERED_FIRST,
                FIELD_LAST_NAME,  REGISTERED_LAST,
                FIELD_BIRTHDATE,  REGISTERED_DOB))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(201);
    }

    // ── Validation — bean constraints (400) ───────────────────────────────────

    @Test
    void register_withMissingFirstName_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,  USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,     EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,  VALID_PASSWORD,
                FIELD_LAST_NAME, REGISTERED_LAST,
                FIELD_BIRTHDATE, REGISTERED_DOB))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(400);
    }

    @Test
    void register_withMissingBirthdate_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, REGISTERED_FIRST,
                FIELD_LAST_NAME,  REGISTERED_LAST))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(400);
    }

    @Test
    void register_withPasswordMissingSpecialCharacter_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   WEAK_PASSWORD,
                FIELD_FIRST_NAME, REGISTERED_FIRST,
                FIELD_LAST_NAME,  REGISTERED_LAST,
                FIELD_BIRTHDATE,  REGISTERED_DOB))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(400);
    }

    @Test
    void register_withInvalidEmailFormat_returns400() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      INVALID_EMAIL,
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, REGISTERED_FIRST,
                FIELD_LAST_NAME,  REGISTERED_LAST,
                FIELD_BIRTHDATE,  REGISTERED_DOB))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(400);
    }

    // ── Domain errors ─────────────────────────────────────────────────────────

    @Test
    void register_withNoMatchingPatient_returns422() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, "Unknown",
                FIELD_LAST_NAME,  "Patient",
                FIELD_BIRTHDATE,  "1900-01-01"))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(422);
    }

    @Test
    void register_withAlreadyRegisteredPatient_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, REGISTERED_FIRST,
                FIELD_LAST_NAME,  REGISTERED_LAST,
                FIELD_BIRTHDATE,  REGISTERED_DOB))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(409);
    }

    @Test
    void register_withAlreadyTakenUsername_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   VALID_USED_USERNAME,
                FIELD_EMAIL,      VALID_USED_EMAIL,
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, "Unknown",
                FIELD_LAST_NAME,  "Patient",
                FIELD_BIRTHDATE,  "1900-01-01"))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(409);
    }

    @Test
    void register_withAlreadyTakenEmail_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      VALID_USED_EMAIL,
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_FIRST_NAME, "Unknown",
                FIELD_LAST_NAME,  "Patient",
                FIELD_BIRTHDATE,  "1900-01-01"))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(409);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
