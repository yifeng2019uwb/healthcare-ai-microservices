package com.healthcare.dao;

import com.healthcare.entity.AiAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiAnalysisResultDao extends JpaRepository<AiAnalysisResult, UUID> {

    /**
     * Most recent analysis for a patient — used for snapshot diff before calling Gemini.
     */
    Optional<AiAnalysisResult> findTopByPatientIdOrderByGeneratedAtDesc(UUID patientId);

    /**
     * Full history for a patient — used by the governance/audit API.
     */
    List<AiAnalysisResult> findByPatientIdOrderByGeneratedAtDesc(UUID patientId);
}
