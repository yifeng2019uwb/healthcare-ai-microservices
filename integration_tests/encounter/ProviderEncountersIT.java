package encounter;

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

@Disabled("Appointment service deleted — re-enable when service is restored")
@DisplayName("Provider Encounter Endpoints")
class ProviderEncountersIT extends BaseIT {

    // ── Test data ─────────────────────────────────────────────────────────────
    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";
    private static final String MALFORMED_ID    = "not-a-uuid";

    private static String encounterId;
    private static String patientId;

    @BeforeAll
    static void fetchIds() {
        waitUntilReady(LoginHelper.providerToken(), ApiPaths.ENCOUNTERS_PROVIDER);

        Response listResp = LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER)
            .then().extract().response();
        if (listResp.statusCode() == 200) {
            encounterId = listResp.path("encounters[0].id");
        }

        if (encounterId != null) {
            Response detailResp = LoginHelper.asProvider()
                .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/" + encounterId)
                .then().extract().response();
            if (detailResp.statusCode() == 200) {
                patientId = detailResp.path("patient.id");
            }
        }
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getEncounterList_asProvider_returns200() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("total",      notNullValue())
            .body("encounters", notNullValue());
    }

    @Test
    void getEncounterDetail_asProvider_returns200() {
        assumeTrue(encounterId != null, "No encounter ID available — provider has no encounters");
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/" + encounterId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue());
    }

    @Test
    void getPatientEncounters_asProvider_returns200() {
        assumeTrue(patientId != null, "No patient ID available — missing from encounter detail");
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/patients/" + patientId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("encounters", notNullValue());
    }

    // ── Security ─────────────────────────────────────────────────────────────

    @Test
    void getEncounterList_withoutToken_returns401() {
        given().when().get(ApiPaths.ENCOUNTERS_PROVIDER).then().statusCode(401);
    }

    // ── Validation — /provider/{id} ───────────────────────────────────────────

    @Test
    void getEncounterDetail_withNonExistentId_returns404() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/" + NON_EXISTENT_ID)
            .then().statusCode(404);
    }

    @Test
    void getEncounterDetail_withMalformedId_returns400() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/" + MALFORMED_ID)
            .then().statusCode(400);
    }

    // ── Validation — /provider/patients/{patientId} ───────────────────────────

    @Test
    void getPatientEncounters_withNonExistentPatientId_returns404() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/patients/" + NON_EXISTENT_ID)
            .then().statusCode(404);
    }

    @Test
    void getPatientEncounters_withMalformedPatientId_returns400() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.ENCOUNTERS_PROVIDER + "/patients/" + MALFORMED_ID)
            .then().statusCode(400);
    }
}
