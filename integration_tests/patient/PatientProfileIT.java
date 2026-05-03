package patient;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.ApiPaths;
import util.BaseIT;
import util.LoginHelper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Patient Profile Endpoints")
class PatientProfileIT extends BaseIT {

    @BeforeAll
    static void setUp() {
        waitUntilReady(LoginHelper.patientToken(), ApiPaths.PATIENT_ME);
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getProfile_asPatient_returns200() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.PATIENT_ME)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id",                  notNullValue())
            .body("identifier[0].value", notNullValue())
            .body("name[0].family",      notNullValue());
    }

    @Test
    void getEncounterList_asPatient_returns200() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.PATIENT_ENCOUNTERS)
            .then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    void getConditionList_asPatient_returns200() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.PATIENT_CONDITIONS)
            .then().statusCode(200).contentType(ContentType.JSON);
    }

    @Test
    void getAllergyList_asPatient_returns200() {
        LoginHelper.asPatient()
            .when().get(ApiPaths.PATIENT_ALLERGIES)
            .then().statusCode(200).contentType(ContentType.JSON);
    }

    // ── Security ─────────────────────────────────────────────────────────────

    @Test
    void getProfile_withoutToken_returns401() {
        given().when().get(ApiPaths.PATIENT_ME).then().statusCode(401);
    }

    @Test
    void getEncounterList_withoutToken_returns401() {
        given().when().get(ApiPaths.PATIENT_ENCOUNTERS).then().statusCode(401);
    }

    @Test
    void getConditionList_withoutToken_returns401() {
        given().when().get(ApiPaths.PATIENT_CONDITIONS).then().statusCode(401);
    }

    @Test
    void getAllergyList_withoutToken_returns401() {
        given().when().get(ApiPaths.PATIENT_ALLERGIES).then().statusCode(401);
    }
}
