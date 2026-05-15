package admin;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import util.ApiPaths;
import util.BaseIT;
import util.LoginHelper;
import util.TestAccounts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Admin Import Endpoints")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminImportIT extends BaseIT {

    private static final Path CSV_DIR = Path.of(
            System.getProperty("test.csv.dir", "test-data/csv"));

    private static String adminToken;

    @BeforeAll
    static void setUp() {
        adminToken = LoginHelper.adminToken();
    }

    // ── Step 1: Admin login ───────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Admin login returns 200 with token")
    void login_withAdminCredentials_returns200WithToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"username": "%s", "password": "%s"}
                """.formatted(TestAccounts.ADMIN_USERNAME, TestAccounts.ADMIN_PASSWORD))
        .when()
            .post(ApiPaths.LOGIN)
        .then()
            .statusCode(200)
            .body("access_token",  notNullValue())
            .body("token_type",    equalTo("Bearer"));
    }

    // ── Step 2: Security ─────────────────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("Import without token returns 401")
    void importOrganizations_withoutToken_returns401() {
        given()
            .multiPart("file", "organizations.csv", new byte[0], "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_ORGANIZATIONS)
        .then()
            .statusCode(401);
    }

    @Test
    @Order(3)
    @DisplayName("Import with patient token returns 403")
    void importOrganizations_withPatientToken_returns403() {
        LoginHelper.asPatient()
            .multiPart("file", "organizations.csv", new byte[0], "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_ORGANIZATIONS)
        .then()
            .statusCode(403);
    }

    // ── Step 3: All import APIs (FK order) ────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("Import organizations returns 200 with ImportResult")
    void importOrganizations_withValidCsv_returns200() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "organizations.csv", csvBytes("organizations.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_ORGANIZATIONS)
        .then()
            .statusCode(200)
            .body("total", greaterThan(0));
    }

    @Test
    @Order(20)
    @DisplayName("Import patients returns 200 with ImportResult")
    void importPatients_withValidCsv_returns200() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "patients.csv", csvBytes("patients.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_PATIENTS)
        .then()
            .statusCode(200)
            .body("total", greaterThan(0));
    }

    @Test
    @Order(30)
    @DisplayName("Import providers returns 200 with ImportResult")
    void importProviders_withValidCsv_returns200() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "providers.csv", csvBytes("providers.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_PROVIDERS)
        .then()
            .statusCode(200)
            .body("total", greaterThan(0));
    }

    @Test
    @Order(40)
    @DisplayName("Import encounters returns 200 with ImportResult")
    void importEncounters_withValidCsv_returns200() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "encounters.csv", csvBytes("encounters.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_ENCOUNTERS)
        .then()
            .statusCode(200)
            .body("total", greaterThan(0));
    }

    @Test
    @Order(50)
    @DisplayName("Import conditions returns 200 with ImportResult")
    void importConditions_withValidCsv_returns200() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "conditions.csv", csvBytes("conditions.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_CONDITIONS)
        .then()
            .statusCode(200)
            .body("total", greaterThan(0));
    }

    @Test
    @Order(60)
    @DisplayName("Import allergies returns 200 with ImportResult")
    void importAllergies_withValidCsv_returns200() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "allergies.csv", csvBytes("allergies.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_ALLERGIES)
        .then()
            .statusCode(200)
            .body("total", greaterThan(0));
    }

    // ── Step 4: Idempotency — re-import same data ─────────────────────────────

    @Test
    @Order(70)
    @DisplayName("Re-importing organizations skips duplicates")
    void importOrganizations_reimport_skipsAllRows() throws IOException {
        LoginHelper.withToken(adminToken)
            .multiPart("file", "organizations.csv", csvBytes("organizations.csv"), "text/csv")
        .when()
            .post(ApiPaths.ADMIN_IMPORT_ORGANIZATIONS)
        .then()
            .statusCode(200)
            .body("imported", equalTo(0));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static byte[] csvBytes(String filename) throws IOException {
        return Files.readAllBytes(CSV_DIR.resolve(filename));
    }
}
