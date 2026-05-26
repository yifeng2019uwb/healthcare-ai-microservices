package com.healthcare.service;

import com.healthcare.dto.AiAnalysisResponse;

import java.util.List;
import java.util.UUID;

public interface AiAnalysisService {

    /**
     * Provider requests AI analysis for an encounter on demand.
     * ADMIN role bypasses ownership check, using encounter.getProviderId() as effective provider.
     * PROVIDER role requires encounter.getProviderId() == requesterId.
     * Runs analysis synchronously and returns the full result.
     */
    AiAnalysisResponse requestAnalysis(UUID encounterId, UUID requesterId, String requesterRole);

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
