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
    private static final String FIELD_MRN        = "mrn";
    private static final String FIELD_FIRST_NAME = "first_name";
    private static final String FIELD_LAST_NAME  = "last_name";

    // ── Known registered account (seed data) ──────────────────────────────────
    private static final String EXISTING_USERNAME  = TestAccounts.PATIENT_USERNAME;
    private static final String EXISTING_EMAIL     = TestAccounts.PATIENT_EMAIL;
    private static final String REGISTERED_MRN     = TestAccounts.PATIENT_MRN;
    private static final String REGISTERED_FIRST   = TestAccounts.PATIENT_FIRST_NAME;
    private static final String REGISTERED_LAST    = TestAccounts.PATIENT_LAST_NAME;

    // ── Happy path patient (override via system properties) ───────────────────
    private static final String UNREGISTERED_MRN   = System.getProperty("test.patient.mrn",       "MRN001");
    private static final String UNREGISTERED_FIRST = System.getProperty("test.patient.firstName", "John");
    private static final String UNREGISTERED_LAST  = System.getProperty("test.patient.lastName",  "Doe");

    // ── Placeholder values for fields not under test ──────────────────────────
    private static final String PLACEHOLDER_MRN    = "MRN001";
    private static final String PLACEHOLDER_FIRST  = "John";
    private static final String PLACEHOLDER_LAST   = "Doe";

    // ── Valid / invalid input constants ───────────────────────────────────────
    private static final String VALID_PASSWORD     = "Password1@";
    private static final String WEAK_PASSWORD      = "Password123";   // missing special char
    private static final String INVALID_EMAIL      = "not-an-email";
    private static final String NON_EXISTENT_MRN   = "MRN-DOES-NOT-EXIST";
    private static final String WRONG_FIRST_NAME   = "Wrong";
    private static final String WRONG_LAST_NAME    = "Name";

    // ── Username / email format patterns ──────────────────────────────────────
    private static final String USERNAME_PATTERN   = "user_%s";
    private static final String EMAIL_PATTERN      = "e_%s@example.com";

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @Disabled("Stateful: requires an unregistered patient in DB. " +
              "Set -Dtest.patient.mrn / firstName / lastName then re-enable once per environment.")
    void register_withValidUnregisteredPatient_returns201() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   "test_reg_" + suffix,
                FIELD_EMAIL,      "reg_" + suffix + "@example.com",
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_MRN,        UNREGISTERED_MRN,
                FIELD_FIRST_NAME, UNREGISTERED_FIRST,
                FIELD_LAST_NAME,  UNREGISTERED_LAST))
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
                FIELD_MRN,       PLACEHOLDER_MRN,
                FIELD_LAST_NAME, PLACEHOLDER_LAST))
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
                FIELD_MRN,        PLACEHOLDER_MRN,
                FIELD_FIRST_NAME, PLACEHOLDER_FIRST,
                FIELD_LAST_NAME,  PLACEHOLDER_LAST))
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
                FIELD_MRN,        PLACEHOLDER_MRN,
                FIELD_FIRST_NAME, PLACEHOLDER_FIRST,
                FIELD_LAST_NAME,  PLACEHOLDER_LAST))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(400);
    }

    // ── Domain errors (404 / 409) ─────────────────────────────────────────────

    @Test
    void register_withNonExistentMrn_returns404() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_MRN,        NON_EXISTENT_MRN,
                FIELD_FIRST_NAME, PLACEHOLDER_FIRST,
                FIELD_LAST_NAME,  PLACEHOLDER_LAST))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(404);
    }

    @Test
    void register_withAlreadyTakenUsername_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   EXISTING_USERNAME,
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_MRN,        PLACEHOLDER_MRN,
                FIELD_FIRST_NAME, PLACEHOLDER_FIRST,
                FIELD_LAST_NAME,  PLACEHOLDER_LAST))
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
                FIELD_EMAIL,      EXISTING_EMAIL,
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_MRN,        PLACEHOLDER_MRN,
                FIELD_FIRST_NAME, PLACEHOLDER_FIRST,
                FIELD_LAST_NAME,  PLACEHOLDER_LAST))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(409);
    }

    @Test
    void register_withAlreadyRegisteredMrn_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_MRN,        REGISTERED_MRN,
                FIELD_FIRST_NAME, REGISTERED_FIRST,
                FIELD_LAST_NAME,  REGISTERED_LAST))
        .when()
            .post(ApiPaths.REGISTER_PATIENT)
        .then()
            .statusCode(409);
    }

    @Test
    void register_withWrongNameForValidMrn_returns409() {
        String suffix = uniqueSuffix();
        given()
            .contentType(ContentType.JSON)
            .body(Map.of(
                FIELD_USERNAME,   USERNAME_PATTERN.formatted(suffix),
                FIELD_EMAIL,      EMAIL_PATTERN.formatted(suffix),
                FIELD_PASSWORD,   VALID_PASSWORD,
                FIELD_MRN,        REGISTERED_MRN,
                FIELD_FIRST_NAME, WRONG_FIRST_NAME,
                FIELD_LAST_NAME,  WRONG_LAST_NAME))
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
