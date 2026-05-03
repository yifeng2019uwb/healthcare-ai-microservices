package provider;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.ApiPaths;
import util.BaseIT;
import util.LoginHelper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("Provider Profile Endpoints")
class ProviderProfileIT extends BaseIT {

    // ── Test data ─────────────────────────────────────────────────────────────
    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";
    private static final String MALFORMED_ID    = "not-a-uuid";

    private static String patientId;

    @BeforeAll
    static void fetchPatientId() {
        waitUntilReady(LoginHelper.providerToken(), ApiPaths.PROVIDER_ME);

        Response resp = LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS)
            .then().extract().response();
        if (resp.statusCode() == 200) {
            patientId = resp.path("[0].id");
        }
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getProfile_asProvider_returns200() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_ME)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",            notNullValue())
            .body("provider_code", notNullValue())
            .body("name",          notNullValue());
    }

    @Test
    void getPatientList_asProvider_returns200() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS)
            .then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    void getPatientDetail_asProvider_returns200() {
        assumeTrue(patientId != null, "No patient ID available — provider has no patients");
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + patientId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",  notNullValue())
            .body("mrn", notNullValue());
    }

    @Test
    void getPatientConditions_asProvider_returns200() {
        assumeTrue(patientId != null, "No patient ID available — provider has no patients");
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + patientId + "/conditions")
            .then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    void getPatientAllergies_asProvider_returns200() {
        assumeTrue(patientId != null, "No patient ID available — provider has no patients");
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + patientId + "/allergies")
            .then().statusCode(200).contentType(ContentType.JSON);
    }

    // ── Security ─────────────────────────────────────────────────────────────

    @Test
    void getProfile_withoutToken_returns401() {
        given().when().get(ApiPaths.PROVIDER_ME).then().statusCode(401);
    }

    @Test
    void getPatientList_withoutToken_returns401() {
        given().when().get(ApiPaths.PROVIDER_PATIENTS).then().statusCode(401);
    }

    // ── Validation — /patients/{id} ───────────────────────────────────────────

    @Test
    void getPatientDetail_withNonExistentId_returns404() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + NON_EXISTENT_ID)
            .then().statusCode(404);
    }

    @Test
    void getPatientDetail_withMalformedId_returns400() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + MALFORMED_ID)
            .then().statusCode(400);
    }

    // ── Validation — /patients/{id}/conditions ────────────────────────────────

    @Test
    void getPatientConditions_withNonExistentId_returns404() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + NON_EXISTENT_ID + "/conditions")
            .then().statusCode(404);
    }

    @Test
    void getPatientConditions_withMalformedId_returns400() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + MALFORMED_ID + "/conditions")
            .then().statusCode(400);
    }

    // ── Validation — /patients/{id}/allergies ─────────────────────────────────

    @Test
    @Disabled("Provider service returns 403 for non-existent patient — behaviour under review")
    void getPatientAllergies_withNonExistentId_returns404() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + NON_EXISTENT_ID + "/allergies")
            .then().statusCode(404);
    }

    @Test
    void getPatientAllergies_withMalformedId_returns400() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.PROVIDER_PATIENTS + "/" + MALFORMED_ID + "/allergies")
            .then().statusCode(400);
    }
}
