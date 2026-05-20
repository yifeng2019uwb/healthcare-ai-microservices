package com.healthcare.dao;

import com.healthcare.entity.AiAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiAnalysisResultDao extends JpaRepository<AiAnalysisResult, UUID> {

    /**
     * Most recent analysis for a patient — used by the read API and freshness check.
     */
    Optional<AiAnalysisResult> findTopByPatientIdOrderByGeneratedAtDesc(UUID patientId);

    /**
     * Freshness check before calling Gemini: returns true if a result exists
     * generated after the given threshold.
     */
    boolean existsByPatientIdAndGeneratedAtAfter(UUID patientId, OffsetDateTime threshold);
}
