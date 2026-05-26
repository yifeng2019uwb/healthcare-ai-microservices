package com.healthcare.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.constants.SecurityConstants;
import com.healthcare.dao.AiAnalysisResultDao;
import com.healthcare.dao.AllergyDao;
import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.ConditionDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dto.AiAnalysisResponse;
import com.healthcare.dto.ClinicalSnapshot;
import com.healthcare.dto.GeminiAnalysisResult;
import com.healthcare.dto.RiskFlag;
import com.healthcare.dto.SnapshotItem;
import com.healthcare.entity.AiAnalysisResult;
import com.healthcare.entity.Allergy;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Condition;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.AiTriggerType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.AiServiceException;
import com.healthcare.service.AiAnalysisService;
import com.healthcare.service.GeminiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisServiceImpl.class);

    private static final String RESOURCE_AI = "ai_analysis_results";
    private static final String DISCLAIMER =
            "AI-generated for informational purposes only. Not a diagnosis or treatment recommendation.";

    private final AiAnalysisResultDao aiAnalysisResultDao;
    private final PatientDao          patientDao;
    private final ConditionDao        conditionDao;
    private final AllergyDao          allergyDao;
    private final EncounterDao        encounterDao;
    private final AuditLogDao         auditLogDao;
    private final GeminiClient        geminiClient;
    private final ObjectMapper        objectMapper;

    @Value("${gemini.model:gemini-1.5-pro}")
    private String modelVersion;

    public AiAnalysisServiceImpl(AiAnalysisResultDao aiAnalysisResultDao,
                                  PatientDao patientDao,
                                  ConditionDao conditionDao,
                                  AllergyDao allergyDao,
                                  EncounterDao encounterDao,
                                  AuditLogDao auditLogDao,
                                  GeminiClient geminiClient,
                                  ObjectMapper objectMapper) {
        this.aiAnalysisResultDao = aiAnalysisResultDao;
        this.patientDao          = patientDao;
        this.conditionDao        = conditionDao;
        this.allergyDao          = allergyDao;
        this.encounterDao        = encounterDao;
        this.auditLogDao         = auditLogDao;
        this.geminiClient        = geminiClient;
        this.objectMapper        = objectMapper;
    }

    @Override
    @Transactional
    public AiAnalysisResponse requestAnalysis(UUID encounterId, UUID requesterId, String requesterRole) {
        Encounter encounter = encounterDao.findById(encounterId)
                .orElseThrow(() -> new AiServiceException(
                        HttpStatus.NOT_FOUND,
                        AiServiceException.ENCOUNTER_NOT_FOUND,
                        "Encounter not found: " + encounterId));

        UUID effectiveProviderId;
        if (SecurityConstants.ROLE_ADMIN.equals(requesterRole)) {
            effectiveProviderId = encounter.getProviderId();
        } else {
            if (!requesterId.equals(encounter.getProviderId())) {
                throw new AiServiceException(
                        HttpStatus.FORBIDDEN,
                        AiServiceException.PROVIDER_NOT_AUTHORIZED,
                        "Provider not associated with encounter: " + encounterId);
            }
            effectiveProviderId = requesterId;
        }

        UUID patientId = encounter.getPatientId();
        runAnalysis(patientId, encounterId, AiTriggerType.MANUAL, null);

        AiAnalysisResult result = aiAnalysisResultDao
                .findTopByPatientIdOrderByGeneratedAtDesc(patientId)
                .orElseThrow(() -> new AiServiceException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        AiServiceException.INTERNAL_ERROR,
                        "No analysis result available for patient: " + patientId));

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_AI, Outcome.SUCCESS)
                .withAuthId(effectiveProviderId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patientId));

        return toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public AiAnalysisResponse getLatestAnalysisForPatient(UUID patientId, UUID providerId) {
        AiAnalysisResult result = aiAnalysisResultDao
                .findTopByPatientIdOrderByGeneratedAtDesc(patientId)
                .orElseThrow(() -> new AiServiceException(
                        HttpStatus.NOT_FOUND,
                        AiServiceException.NO_ANALYSIS_FOUND,
                        "No analysis found for patient: " + patientId));

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_AI, Outcome.SUCCESS)
                .withAuthId(providerId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patientId));

        return toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiAnalysisResponse> getPatientHistory(UUID patientId, UUID providerId) {
        List<AiAnalysisResult> results = aiAnalysisResultDao
                .findByPatientIdOrderByGeneratedAtDesc(patientId);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_AI, Outcome.SUCCESS)
                .withAuthId(providerId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patientId));

        return results.stream().map(this::toResponse).toList();
    }

    // -------------------------------------------------------------------------

    private void runAnalysis(UUID patientId, UUID encounterId,
                              AiTriggerType triggerType, String triggerCode) {
        Patient patient = patientDao.findById(patientId).orElse(null);
        if (patient == null) {
            log.warn("runAnalysis: patient not found, skipping. patientId={}", patientId);
            return;
        }

        List<Condition> conditions = conditionDao.findByIdPatientId(patientId);
        List<Allergy>   allergies  = allergyDao.findByIdPatientId(patientId);
        List<Encounter> encounters = encounterDao.findByPatientId(patientId);

        Map<UUID, LocalDate> encounterDates = encounters.stream()
                .filter(e -> e.getStartTime() != null)
                .collect(Collectors.toMap(
                        Encounter::getId,
                        e -> e.getStartTime().toLocalDate(),
                        (a, b) -> a));

        List<SnapshotItem> conditionItems = buildConditionItems(conditions, encounterDates);
        List<SnapshotItem> allergyItems   = buildAllergyItems(allergies, encounterDates);

        if (snapshotUnchanged(patientId, conditionItems, allergyItems)) {
            log.debug("Snapshot unchanged for patient={}, skipping Gemini call", patientId);
            return;
        }

        ClinicalSnapshot snapshot = new ClinicalSnapshot(triggerCode, conditionItems, allergyItems);
        String inputRecordIds = toJson(snapshot);

        String prompt = buildPrompt(patient, conditions, allergies, encounters);
        GeminiAnalysisResult geminiResult = geminiClient.analyze(prompt);

        AiAnalysisResult result = new AiAnalysisResult(
                patientId,
                geminiResult.summary(),
                toJson(geminiResult.riskFlags()),
                triggerType,
                null,
                modelVersion,
                inputRecordIds,
                encounterId);

        aiAnalysisResultDao.save(result);
        log.info("AI analysis saved: patient={}, encounter={}, trigger={}",
                patientId, encounterId, triggerType);
    }

    private boolean snapshotUnchanged(UUID patientId,
                                       List<SnapshotItem> conditionItems,
                                       List<SnapshotItem> allergyItems) {
        Optional<AiAnalysisResult> lastOpt =
                aiAnalysisResultDao.findTopByPatientIdOrderByGeneratedAtDesc(patientId);
        if (lastOpt.isEmpty()) return false;

        try {
            JsonNode root = objectMapper.readTree(lastOpt.get().getInputRecordIds());
            return itemFingerprints(conditionItems).equals(fingerprintsFromNode(root.path("conditions")))
                    && itemFingerprints(allergyItems).equals(fingerprintsFromNode(root.path("allergies")));
        } catch (Exception e) {
            log.warn("Snapshot comparison failed, proceeding with analysis: {}", e.getMessage());
            return false;
        }
    }

    private Set<String> itemFingerprints(List<SnapshotItem> items) {
        return items.stream()
                .map(i -> i.code() + "|" + str(i.startDate()) + "|" + str(i.stopDate()) + "|" + str(i.encounterDate()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<String> fingerprintsFromNode(JsonNode array) {
        Set<String> result = new HashSet<>();
        if (!array.isArray()) return result;
        for (JsonNode node : array) {
            result.add(nodeText(node, "code")
                    + "|" + nodeText(node, "start_date", "startDate")
                    + "|" + nodeText(node, "stop_date", "stopDate")
                    + "|" + nodeText(node, "encounter_date", "encounterDate"));
        }
        return result;
    }

    private String nodeText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode child = node.path(key);
            if (!child.isMissingNode()) return child.isNull() ? "null" : child.asText();
        }
        return "null";
    }

    private String str(String value) {
        return value != null ? value : "null";
    }

    private List<SnapshotItem> buildConditionItems(List<Condition> conditions,
                                                    Map<UUID, LocalDate> encounterDates) {
        return conditions.stream()
                .map(c -> new SnapshotItem(
                        c.getCode(),
                        c.getDescription(),
                        c.getStartDate() != null ? c.getStartDate().toString() : null,
                        c.getStopDate()  != null ? c.getStopDate().toString()  : null,
                        dateString(encounterDates.get(c.getId().getEncounterId()))))
                .sorted(Comparator.comparing(SnapshotItem::code))
                .toList();
    }

    private List<SnapshotItem> buildAllergyItems(List<Allergy> allergies,
                                                  Map<UUID, LocalDate> encounterDates) {
        return allergies.stream()
                .map(a -> new SnapshotItem(
                        a.getId().getCode(),
                        a.getDescription(),
                        a.getStartDate() != null ? a.getStartDate().toString() : null,
                        a.getStopDate()  != null ? a.getStopDate().toString()  : null,
                        dateString(encounterDates.get(a.getId().getEncounterId()))))
                .sorted(Comparator.comparing(SnapshotItem::code))
                .toList();
    }

    private String dateString(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    private String buildPrompt(Patient patient, List<Condition> conditions,
                                List<Allergy> allergies, List<Encounter> encounters) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyze the following patient medical history and provide a clinical summary and risk flags.\n\n");
        sb.append("Patient: ").append(patient.getFirstName()).append(" ").append(patient.getLastName());
        if (patient.getBirthdate() != null) {
            sb.append(", DOB: ").append(patient.getBirthdate());
        }
        sb.append("\n\nConditions (").append(conditions.size()).append("):\n");
        for (Condition c : conditions) {
            sb.append("- ").append(c.getCode());
            if (c.getDescription() != null) sb.append(": ").append(c.getDescription());
            if (c.getStartDate() != null) sb.append(" (since ").append(c.getStartDate()).append(")");
            sb.append("\n");
        }
        sb.append("\nAllergies (").append(allergies.size()).append("):\n");
        for (Allergy a : allergies) {
            sb.append("- ").append(a.getDescription() != null ? a.getDescription() : a.getId().getCode());
            if (a.getReaction1() != null) sb.append(", reaction: ").append(a.getReaction1());
            if (a.getSeverity1() != null) sb.append(", severity: ").append(a.getSeverity1());
            sb.append("\n");
        }
        sb.append("\nRecent encounters (").append(encounters.size()).append("):\n");
        for (Encounter e : encounters) {
            sb.append("- ").append(e.getEncounterType() != null ? e.getEncounterType() : "Visit");
            if (e.getStartTime() != null) sb.append(" on ").append(e.getStartTime().toLocalDate());
            sb.append("\n");
        }
        sb.append("\nRespond ONLY with valid JSON, no markdown:\n");
        sb.append("{\"summary\": \"...\", \"risk_flags\": [{\"flag\": \"...\", \"reason\": \"...\"}], ");
        sb.append("\"disclaimer\": \"").append(DISCLAIMER).append("\"}");
        return sb.toString();
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private AiAnalysisResponse toResponse(AiAnalysisResult result) {
        return new AiAnalysisResponse(
                result.getPatientId(),
                result.getLastEncounterId(),
                result.getGeneratedAt(),
                result.getSummary(),
                parseRiskFlags(result.getRiskFlags()),
                DISCLAIMER,
                result.getModelVersion(),
                result.getTriggerType());
    }

    @SuppressWarnings("unchecked")
    private List<RiskFlag> parseRiskFlags(String riskFlagsJson) {
        try {
            return objectMapper.readValue(riskFlagsJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RiskFlag.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse risk_flags JSON: {}", e.getMessage());
            return List.of();
        }
    }
}
