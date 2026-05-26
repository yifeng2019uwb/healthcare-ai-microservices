package com.healthcare.service;

import com.healthcare.dto.AiAnalysisResponse;

import java.util.List;
import java.util.UUID;

public interface AiAnalysisService {

    /**
     * Provider requests AI analysis for an encounter on demand.
     * Validates the provider (by authId) owns the encounter before running analysis.
     */
    AiAnalysisResponse requestAnalysis(UUID encounterId, UUID authId);

    /**
     * Returns the most recent analysis result for the patient across all encounters.
     * Throws AiServiceException(404) if none exists yet.
     */
    AiAnalysisResponse getLatestAnalysisForPatient(UUID patientId, UUID providerId);

    /**
     * Returns all analysis results for the patient, newest first.
     * Returns empty list if none exist yet.
     */
    List<AiAnalysisResponse> getPatientHistory(UUID patientId, UUID providerId);
}
