package ai;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import util.ApiPaths;
import util.BaseIT;
import util.LoginHelper;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for provider condition writes and AI analysis requests.
 *
 * Provider: drDouglass (provider_id = 7d7fef4e-36c5-30e2-a2f3-4664e57f7008)
 * Patient:  a1a0f8df-6611-26d4-798a-b839a861bf83
 */
@DisplayName("AI Analysis — condition write + on-demand trigger")
class AiAnalysisIT extends BaseIT {

    // Encounters belonging to drDouglass / same patient
    private static final String ENCOUNTER_2026 = "a1a0f8df-6611-26d4-b387-394e82e61774"; // 2026-05-13 check up
    private static final String ENCOUNTER_2025 = "a1a0f8df-6611-26d4-8afa-43dc95110b35"; // 2025-05-07 check up
    private static final String PATIENT_ID     = "a1a0f8df-6611-26d4-798a-b839a861bf83";

    private static final String NON_EXISTENT_ENCOUNTER = "00000000-0000-0000-0000-000000000000";

    @BeforeAll
    static void setUp() {
        waitUntilReady(LoginHelper.providerToken(), ApiPaths.PROVIDER_ME);
    }

    // ── Add condition ─────────────────────────────────────────────────────────

    @Test
    void addCondition_asProvider_returns201() {
        LoginHelper.asProvider()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "code": "44054006",
                    "description": "Diabetes mellitus type 2",
                    "start_date": "2026-05-13"
                }
                """)
            .when().post(ApiPaths.PROVIDER_ENCOUNTER_CONDITIONS, ENCOUNTER_2026)
            .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("code",   equalTo("44054006"))
            .body("status", equalTo("active"));
    }

    @Test
    void addCondition_secondTime_isIdempotent_returns201() {
        // Composite PK (patientId, encounterId, code) — save() is upsert, safe to re-post
        LoginHelper.asProvider()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "code": "44054006",
                    "description": "Diabetes mellitus type 2",
                    "start_date": "2026-05-13"
                }
                """)
            .when().post(ApiPaths.PROVIDER_ENCOUNTER_CONDITIONS, ENCOUNTER_2026)
            .then()
            .statusCode(201);
    }

    @Test
    void addCondition_withNonExistentEncounter_returns404() {
        LoginHelper.asProvider()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "code": "44054006",
                    "description": "Diabetes mellitus type 2",
                    "start_date": "2026-05-13"
                }
                """)
            .when().post(ApiPaths.PROVIDER_ENCOUNTER_CONDITIONS, NON_EXISTENT_ENCOUNTER)
            .then()
            .statusCode(404);
    }

    @Test
    void addCondition_withoutToken_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "code": "44054006",
                    "description": "Diabetes mellitus type 2",
                    "start_date": "2026-05-13"
                }
                """)
            .when().post(ApiPaths.PROVIDER_ENCOUNTER_CONDITIONS, ENCOUNTER_2026)
            .then()
            .statusCode(401);
    }

    @Test
    void addCondition_withMissingRequiredField_returns400() {
        LoginHelper.asProvider()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "description": "Missing code and start_date"
                }
                """)
            .when().post(ApiPaths.PROVIDER_ENCOUNTER_CONDITIONS, ENCOUNTER_2026)
            .then()
            .statusCode(400);
    }

    // ── Request AI analysis ───────────────────────────────────────────────────

    @Test
    @Tag("ai-live")
    void requestAiAnalysis_afterConditionWrite_returns200_withResult() {
        // First write a condition to give Gemini something meaningful to analyze
        LoginHelper.asProvider()
            .contentType(ContentType.JSON)
            .body("""
                {
                    "code": "73211009",
                    "description": "Diabetes mellitus",
                    "start_date": "2025-05-07"
                }
                """)
            .when().post(ApiPaths.PROVIDER_ENCOUNTER_CONDITIONS, ENCOUNTER_2025)
            .then().statusCode(201);

        // Then request AI analysis — calls Gemini, may take 10-30s
        LoginHelper.asProvider()
            .when().post(ApiPaths.AI_REQUEST, ENCOUNTER_2025)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("patient_id",    equalTo(PATIENT_ID))
            .body("summary",       notNullValue())
            .body("risk_flags",    isA(java.util.List.class))
            .body("trigger_type",  equalTo("MANUAL"))
            .body("model_version", notNullValue())
            .body("generated_at",  notNullValue());
    }

    @Test
    void requestAiAnalysis_withNonExistentEncounter_returns404() {
        LoginHelper.asProvider()
            .when().post(ApiPaths.AI_REQUEST, NON_EXISTENT_ENCOUNTER)
            .then()
            .statusCode(404);
    }

    @Test
    void requestAiAnalysis_withoutToken_returns401() {
        given()
            .when().post(ApiPaths.AI_REQUEST, ENCOUNTER_2025)
            .then()
            .statusCode(401);
    }

    // ── Get AI results ────────────────────────────────────────────────────────

    @Test
    void getLatestAiResult_asProvider_returns200() {
        io.restassured.response.Response response = LoginHelper.asProvider()
            .when().get(ApiPaths.AI_LATEST, PATIENT_ID);

        assumeTrue(response.statusCode() != 404,
                "No AI result yet for patient — run ai-live first to populate");

        response.then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("patient_id", equalTo(PATIENT_ID))
            .body("summary",    notNullValue());
    }

    @Test
    void getAiHistory_asProvider_returns200_withList() {
        LoginHelper.asProvider()
            .when().get(ApiPaths.AI_HISTORY, PATIENT_ID)
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", isA(java.util.List.class));
    }

    @Test
    void getLatestAiResult_withoutToken_returns401() {
        given()
            .when().get(ApiPaths.AI_LATEST, PATIENT_ID)
            .then()
            .statusCode(401);
    }
}
