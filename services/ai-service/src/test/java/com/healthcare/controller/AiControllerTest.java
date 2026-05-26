package com.healthcare.controller;

import com.healthcare.dto.AiAnalysisResponse;
import com.healthcare.dto.RiskFlag;
import com.healthcare.enums.AiTriggerType;
import com.healthcare.exception.AiExceptionHandler;
import com.healthcare.exception.AiServiceException;
import com.healthcare.service.AiAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import(AiExceptionHandler.class)
class AiControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  AiAnalysisService aiAnalysisService;

    private static final UUID ENCOUNTER_ID = UUID.randomUUID();
    private static final UUID PATIENT_ID   = UUID.randomUUID();
    private static final UUID PROVIDER_ID  = UUID.randomUUID();

    private AiAnalysisResponse sampleResponse() {
        return new AiAnalysisResponse(
                PATIENT_ID,
                ENCOUNTER_ID,
                OffsetDateTime.now(),
                "Patient has Type 2 diabetes.",
                List.of(new RiskFlag("High readmission risk", "4 ER visits in past 12 months")),
                "AI-generated for informational purposes only.",
                "gemini-1.5-pro",
                AiTriggerType.MANUAL);
    }

    // -------------------------------------------------------------------------
    // POST /api/ai/encounters/{encounterId}/request
    // -------------------------------------------------------------------------

    @Test
    void requestAnalysis_returns200_withResult() throws Exception {
        when(aiAnalysisService.requestAnalysis(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/ai/encounters/{encounterId}/request", ENCOUNTER_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Patient has Type 2 diabetes."))
                .andExpect(jsonPath("$.patient_id").value(PATIENT_ID.toString()))
                .andExpect(jsonPath("$.trigger_type").value("MANUAL"))
                .andExpect(jsonPath("$.model_version").value("gemini-1.5-pro"));
    }

    @Test
    void requestAnalysis_returns403_whenProviderNotOwner() throws Exception {
        when(aiAnalysisService.requestAnalysis(any(), any()))
                .thenThrow(new AiServiceException(
                        HttpStatus.FORBIDDEN,
                        AiServiceException.PROVIDER_NOT_AUTHORIZED,
                        "Provider not associated with encounter"));

        mockMvc.perform(post("/api/ai/encounters/{encounterId}/request", ENCOUNTER_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestAnalysis_returns404_whenEncounterNotFound() throws Exception {
        when(aiAnalysisService.requestAnalysis(any(), any()))
                .thenThrow(new AiServiceException(
                        HttpStatus.NOT_FOUND,
                        AiServiceException.ENCOUNTER_NOT_FOUND,
                        "Encounter not found"));

        mockMvc.perform(post("/api/ai/encounters/{encounterId}/request", ENCOUNTER_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void requestAnalysis_returns400_whenMissingHeaders() throws Exception {
        mockMvc.perform(post("/api/ai/encounters/{encounterId}/request", ENCOUNTER_ID))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/ai/patient/{patientId}
    // -------------------------------------------------------------------------

    @Test
    void getLatestAnalysisForPatient_returns200_withAnalysis() throws Exception {
        when(aiAnalysisService.getLatestAnalysisForPatient(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/ai/patient/{patientId}", PATIENT_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Patient has Type 2 diabetes."))
                .andExpect(jsonPath("$.patient_id").value(PATIENT_ID.toString()))
                .andExpect(jsonPath("$.trigger_type").value("MANUAL"))
                .andExpect(jsonPath("$.model_version").value("gemini-1.5-pro"));
    }

    @Test
    void getLatestAnalysisForPatient_returns404_whenNoAnalysisFound() throws Exception {
        when(aiAnalysisService.getLatestAnalysisForPatient(any(), any()))
                .thenThrow(new AiServiceException(
                        HttpStatus.NOT_FOUND,
                        AiServiceException.NO_ANALYSIS_FOUND,
                        "No analysis found"));

        mockMvc.perform(get("/api/ai/patient/{patientId}", PATIENT_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLatestAnalysisForPatient_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/ai/patient/{patientId}", PATIENT_ID))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/ai/patient/{patientId}/history
    // -------------------------------------------------------------------------

    @Test
    void getPatientHistory_returns200_withList() throws Exception {
        when(aiAnalysisService.getPatientHistory(any(), any()))
                .thenReturn(List.of(sampleResponse(), sampleResponse()));

        mockMvc.perform(get("/api/ai/patient/{patientId}/history", PATIENT_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].trigger_type").value("MANUAL"));
    }

    @Test
    void getPatientHistory_returns200_emptyList() throws Exception {
        when(aiAnalysisService.getPatientHistory(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/patient/{patientId}/history", PATIENT_ID)
                        .header("X-User-Id", PROVIDER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getPatientHistory_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/ai/patient/{patientId}/history", PATIENT_ID))
                .andExpect(status().isBadRequest());
    }
}
