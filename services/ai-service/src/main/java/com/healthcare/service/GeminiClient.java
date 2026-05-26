package com.healthcare.service;

import com.healthcare.dto.GeminiAnalysisResult;

public interface GeminiClient {

    /**
     * Sends the prompt to Gemini and returns the parsed analysis result.
     * Throws AiServiceException(GEMINI_ERROR) on any failure.
     */
    GeminiAnalysisResult analyze(String prompt);
}
