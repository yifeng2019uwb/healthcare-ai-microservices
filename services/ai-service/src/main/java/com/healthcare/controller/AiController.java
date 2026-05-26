package com.healthcare.controller;

import com.healthcare.constants.SecurityConstants;
import com.healthcare.dto.AiAnalysisResponse;
import com.healthcare.service.AiAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiAnalysisService aiAnalysisService;

    public AiController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @PostMapping("/encounters/{encounterId}/request")
    public ResponseEntity<AiAnalysisResponse> requestAnalysis(
            @PathVariable UUID encounterId,
            @RequestHeader(SecurityConstants.HEADER_USER_ID)   String userId,
            @RequestHeader(SecurityConstants.HEADER_USER_ROLE) String userRole) {

        return ResponseEntity.ok(aiAnalysisService.requestAnalysis(
                encounterId, UUID.fromString(userId), userRole));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<AiAnalysisResponse> getLatestAnalysisForPatient(
            @PathVariable UUID patientId,
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String providerId) {

        return ResponseEntity.ok(aiAnalysisService.getLatestAnalysisForPatient(
                patientId, UUID.fromString(providerId)));
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<AiAnalysisResponse>> getPatientHistory(
            @PathVariable UUID patientId,
            @RequestHeader(SecurityConstants.HEADER_USER_ID) String providerId) {

        return ResponseEntity.ok(aiAnalysisService.getPatientHistory(
                patientId, UUID.fromString(providerId)));
    }
}
