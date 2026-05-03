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
@DisplayName("Patient Encounter Endpoints")
class PatientEncountersIT extends BaseIT {

    // ── Test data ─────────────────────────────────────────────────────────────
    private static final String NON_EXISTENT_ID = "00000000-0000-0000-0000-000000000000";
    private static final String MALFORMED_ID    = "not-a-uuid";

    private static String encounterId;
    private static String crossPatientEncounterId;

    @BeforeAll
    static void fetchEncounterIds() {
        waitUntilReady(LoginHelper.patientToken(), ApiPaths.ENCOUNTERS_ME);

        Response resp1 = LoginHelper.asPatient()
            .when().get(ApiPaths.ENCOUNTERS_ME)
            .then().extract().response();
        if (resp1.statusCode() == 200) {
            encounterId = resp1.path("encounters[0].id");
        }

        try {
            Response resp2 = LoginHelper.asPatient2()
                .when().get(ApiPaths.ENCOUNTERS_ME)
                .then().extract().response();
            if (resp2.statusCode() == 200) {
                crossPatientEncounterId = resp2.path("encounters[0].id");
            }
        } catch (Throwable e) {
            // patient2 not seeded — cross-patient test will be skipped
        }
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getEncounterList_asPatient_returns200() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.ENCOUNTERS_ME)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("total",      notNullValue())
            .body("page",       notNullValue())
            .body("encounters", notNullValue());
    }

    @Test
    void getEncounterDetail_asPatient_returns200() {
        assumeTrue(encounterId != null, "No encounter ID available — patient has no encounters");
        LoginHelper.asPatient()
            .when().get(ApiPaths.ENCOUNTERS_ME + "/" + encounterId)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", notNullValue());
    }

    // ── Security ─────────────────────────────────────────────────────────────

    @Test
    void getEncounterList_withoutToken_returns401() {
        given().when().get(ApiPaths.ENCOUNTERS_ME).then().statusCode(401);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    void getEncounterDetail_withNonExistentId_returns404() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.ENCOUNTERS_ME + "/" + NON_EXISTENT_ID)
            .then().statusCode(404);
    }

    @Test
    void getEncounterDetail_withMalformedId_returns400() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.ENCOUNTERS_ME + "/" + MALFORMED_ID)
            .then().statusCode(400);
    }

    @Test
    void getEncounterDetail_ofOtherPatient_returns404() {
        assumeTrue(crossPatientEncounterId != null,
            "testpatient02 not seeded or has no encounters — set -Dtest.patient2.* to enable");
        LoginHelper.asPatient()
            .when().get(ApiPaths.ENCOUNTERS_ME + "/" + crossPatientEncounterId)
            .then().statusCode(404);
    }
}
