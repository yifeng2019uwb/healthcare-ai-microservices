package com.healthcare.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.config.GeminiConfig;
import com.healthcare.dto.GeminiAnalysisResult;
import com.healthcare.exception.AiServiceException;
import com.healthcare.service.impl.GeminiClientImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiClientImplTest {

    private GeminiClientImpl client;

    @BeforeEach
    void setUp() {
        GeminiConfig config = new GeminiConfig();
        config.setApiKey("test-key");
        config.setModel("gemini-1.5-pro");
        config.setBaseUrl("https://generativelanguage.googleapis.com");
        client = new GeminiClientImpl(config, new ObjectMapper());
    }

    // -------------------------------------------------------------------------
    // parseGeminiResponse
    // -------------------------------------------------------------------------

    @Test
    void parseGeminiResponse_returnsResult_whenValidResponse() {
        String innerJson = "{\"summary\":\"Patient has Type 2 diabetes.\","
                + "\"risk_flags\":[{\"flag\":\"High readmission risk\",\"reason\":\"4 ER visits\"}],"
                + "\"disclaimer\":\"AI-generated.\"}";
        String rawResponse = buildGeminiApiResponse(innerJson);

        GeminiAnalysisResult result = client.parseGeminiResponse(rawResponse);

        assertThat(result.summary()).isEqualTo("Patient has Type 2 diabetes.");
        assertThat(result.riskFlags()).hasSize(1);
        assertThat(result.riskFlags().get(0).flag()).isEqualTo("High readmission risk");
        assertThat(result.riskFlags().get(0).reason()).isEqualTo("4 ER visits");
        assertThat(result.disclaimer()).isEqualTo("AI-generated.");
    }

    @Test
    void parseGeminiResponse_returnsEmptyRiskFlags_whenNonePresentInJson() {
        String innerJson = "{\"summary\":\"Healthy patient.\",\"risk_flags\":[],\"disclaimer\":\"AI-generated.\"}";
        String rawResponse = buildGeminiApiResponse(innerJson);

        GeminiAnalysisResult result = client.parseGeminiResponse(rawResponse);

        assertThat(result.summary()).isEqualTo("Healthy patient.");
        assertThat(result.riskFlags()).isEmpty();
    }

    @Test
    void parseGeminiResponse_usesDefaultDisclaimer_whenNotInJson() {
        String innerJson = "{\"summary\":\"Summary.\",\"risk_flags\":[]}";
        String rawResponse = buildGeminiApiResponse(innerJson);

        GeminiAnalysisResult result = client.parseGeminiResponse(rawResponse);

        assertThat(result.disclaimer()).contains("AI-generated for informational purposes only");
    }

    @Test
    void parseGeminiResponse_throwsAiServiceException_whenMalformedOuterJson() {
        assertThatThrownBy(() -> client.parseGeminiResponse("{not valid json}"))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    void parseGeminiResponse_throwsAiServiceException_whenNoCandidates() {
        String rawResponse = "{\"candidates\":[]}";

        assertThatThrownBy(() -> client.parseGeminiResponse(rawResponse))
                .isInstanceOf(AiServiceException.class);
    }

    @Test
    void parseGeminiResponse_throwsAiServiceException_whenInnerTextNotJson() {
        String rawResponse = buildGeminiApiResponse("this is not json");

        assertThatThrownBy(() -> client.parseGeminiResponse(rawResponse))
                .isInstanceOf(AiServiceException.class);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private String buildGeminiApiResponse(String innerText) {
        String escaped = innerText.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"" + escaped + "\"}]}}]}";
    }
}
