package com.healthcare.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.config.GeminiConfig;
import com.healthcare.dto.GeminiAnalysisResult;
import com.healthcare.dto.RiskFlag;
import com.healthcare.exception.AiServiceException;
import com.healthcare.service.GeminiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.HttpServerErrorException;

@Component
public class GeminiClientImpl implements GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClientImpl.class);

    private static final String DISCLAIMER =
            "AI-generated for informational purposes only. Not a diagnosis or treatment recommendation.";

    private final RestClient   restClient;
    private final GeminiConfig config;
    private final ObjectMapper objectMapper;

    public GeminiClientImpl(GeminiConfig config, ObjectMapper objectMapper) {
        this.config       = config;
        this.objectMapper = objectMapper;
        this.restClient   = RestClient.create();
    }

    private static final int  MAX_RETRIES    = 3;
    private static final long RETRY_DELAY_MS = 2_000;

    @Override
    public GeminiAnalysisResult analyze(String prompt) {
        try {
            return callWithRetry(config.getModel(), prompt);
        } catch (AiServiceException e) {
            String fallback = config.getFallbackModel();
            if (fallback != null && !fallback.isBlank()) {
                log.warn("Primary model {} unavailable, falling back to {}", config.getModel(), fallback);
                return callWithRetry(fallback, prompt);
            }
            throw e;
        }
    }

    private GeminiAnalysisResult callWithRetry(String model, String prompt) {
        String url = config.getBaseUrl()
                + "/v1/models/" + model
                + ":generateContent?key=" + config.getApiKey();

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        int attempt = 0;
        while (true) {
            try {
                String rawResponse = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                return parseGeminiResponse(rawResponse);

            } catch (AiServiceException e) {
                throw e;
            } catch (HttpServerErrorException e) {
                if (e.getStatusCode().value() == 503 && attempt < MAX_RETRIES) {
                    attempt++;
                    log.warn("Gemini 503 on model={} attempt {}/{}, retrying in {}ms",
                            model, attempt, MAX_RETRIES, RETRY_DELAY_MS * attempt);
                    try { Thread.sleep(RETRY_DELAY_MS * attempt); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    log.error("Gemini API call failed model={}: {}", model, e.getMessage());
                    throw new AiServiceException(
                            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                            AiServiceException.GEMINI_ERROR,
                            "Gemini API call failed: " + e.getMessage());
                }
            } catch (Exception e) {
                log.error("Gemini API call failed model={}: {}", model, e.getMessage(), e);
                throw new AiServiceException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        AiServiceException.GEMINI_ERROR,
                        "Gemini API call failed: " + e.getMessage());
            }
        }
    }

    GeminiAnalysisResult parseGeminiResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode candidates = root.path("candidates");
            JsonNode firstCandidate = (candidates.isArray() && candidates.size() > 0)
                    ? candidates.get(0) : null;
            JsonNode parts = (firstCandidate != null)
                    ? firstCandidate.path("content").path("parts") : null;
            JsonNode firstPart = (parts != null && parts.isArray() && parts.size() > 0)
                    ? parts.get(0) : null;
            JsonNode textNode = (firstPart != null) ? firstPart.path("text") : null;

            if (textNode == null || textNode.isMissingNode()) {
                throw new AiServiceException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                        AiServiceException.GEMINI_ERROR,
                        "Gemini response missing text content");
            }

            String text = textNode.asText().strip();
            if (text.startsWith("```")) {
                text = text.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("```\\s*$", "").strip();
            }
            JsonNode analysisJson = objectMapper.readTree(text);

            String summary    = analysisJson.path("summary").asText();
            String disclaimer = analysisJson.has("disclaimer")
                    ? analysisJson.path("disclaimer").asText()
                    : DISCLAIMER;

            List<RiskFlag> riskFlags = new ArrayList<>();
            JsonNode flags = analysisJson.path("risk_flags");
            if (flags.isArray()) {
                for (JsonNode flag : flags) {
                    riskFlags.add(new RiskFlag(
                            flag.path("flag").asText(),
                            flag.path("reason").asText()));
                }
            }

            return new GeminiAnalysisResult(summary, riskFlags, disclaimer);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new AiServiceException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    AiServiceException.GEMINI_ERROR,
                    "Failed to parse Gemini response: " + e.getMessage());
        }
    }
}
