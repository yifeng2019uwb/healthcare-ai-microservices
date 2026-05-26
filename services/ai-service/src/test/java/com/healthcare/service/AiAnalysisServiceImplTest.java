package com.healthcare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dao.AiAnalysisResultDao;
import com.healthcare.dao.AllergyDao;
import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.ConditionDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dto.AiAnalysisResponse;
import com.healthcare.dto.GeminiAnalysisResult;
import com.healthcare.entity.AiAnalysisResult;
import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.enums.AiTriggerType;
import com.healthcare.exception.AiServiceException;
import com.healthcare.service.impl.AiAnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceImplTest {

    @Mock private AiAnalysisResultDao aiAnalysisResultDao;
    @Mock private PatientDao          patientDao;
    @Mock private ConditionDao        conditionDao;
    @Mock private AllergyDao          allergyDao;
    @Mock private EncounterDao        encounterDao;
    @Mock private ProviderDao         providerDao;
    @Mock private AuditLogDao         auditLogDao;
    @Mock private GeminiClient        geminiClient;

    @InjectMocks
    private AiAnalysisServiceImpl service;

    private final UUID patientId   = UUID.randomUUID();
    private final UUID encounterId = UUID.randomUUID();
    private final UUID authId      = UUID.randomUUID();
    private final UUID providerId  = UUID.randomUUID();

    private Provider mockProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "modelVersion", "gemini-1.5-pro");
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        mockProvider = mock(Provider.class);
        when(mockProvider.getId()).thenReturn(providerId);
    }

    // -------------------------------------------------------------------------
    // requestAnalysis
    // -------------------------------------------------------------------------

    @Test
    void requestAnalysis_callsGemini_andReturnsResult() {
        Encounter encounter = new Encounter(providerId, OffsetDateTime.now());
        encounter.setId(encounterId);
        encounter.setPatientId(patientId);

        Patient patient = new Patient("Jane", "Doe");
        GeminiAnalysisResult geminiResult = new GeminiAnalysisResult("Summary.", List.of(), "AI-generated.");
        AiAnalysisResult saved = new AiAnalysisResult(
                patientId, "Summary.", "[]", AiTriggerType.MANUAL, null, "gemini-1.5-pro", "{}", encounterId);

        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(conditionDao.findByIdPatientId(patientId)).thenReturn(List.of());
        when(allergyDao.findByIdPatientId(patientId)).thenReturn(List.of());
        when(encounterDao.findByPatientId(patientId)).thenReturn(List.of(encounter));
        when(aiAnalysisResultDao.findTopByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(saved));
        when(geminiClient.analyze(any())).thenReturn(geminiResult);

        AiAnalysisResponse response = service.requestAnalysis(encounterId, authId);

        verify(geminiClient).analyze(any());
        verify(aiAnalysisResultDao).save(any(AiAnalysisResult.class));
        assertThat(response.summary()).isEqualTo("Summary.");
        assertThat(response.triggerType()).isEqualTo(AiTriggerType.MANUAL);
        assertThat(response.patientId()).isEqualTo(patientId);
    }

    @Test
    void requestAnalysis_skipsGemini_andReturnsExistingResult_whenSnapshotUnchanged() throws Exception {
        Encounter encounter = new Encounter(providerId, OffsetDateTime.now());
        encounter.setId(encounterId);
        encounter.setPatientId(patientId);

        Patient patient = new Patient("Jane", "Doe");
        Condition condition = new Condition(
                new ConditionId(patientId, encounterId, "E11"), LocalDate.of(2020, 1, 1));
        condition.setDescription("Type 2 diabetes");

        ObjectMapper mapper = new ObjectMapper();
        String existingSnapshot = mapper.writeValueAsString(
                new com.healthcare.dto.ClinicalSnapshot("E11",
                        List.of(new com.healthcare.dto.SnapshotItem(
                                "E11", "Type 2 diabetes", "2020-01-01", null, "2020-01-15")),
                        List.of()));

        AiAnalysisResult lastResult = new AiAnalysisResult(
                patientId, "Old summary.", "[]",
                AiTriggerType.MANUAL, null, "gemini-1.5-pro", existingSnapshot, encounterId);

        Encounter enc = new Encounter(UUID.randomUUID(), OffsetDateTime.of(2020, 1, 15, 0, 0, 0, 0,
                java.time.ZoneOffset.UTC));
        enc.setId(encounterId);
        enc.setPatientId(patientId);

        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findById(encounterId)).thenReturn(Optional.of(encounter));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(conditionDao.findByIdPatientId(patientId)).thenReturn(List.of(condition));
        when(allergyDao.findByIdPatientId(patientId)).thenReturn(List.of());
        when(encounterDao.findByPatientId(patientId)).thenReturn(List.of(enc));
        when(aiAnalysisResultDao.findTopByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(Optional.of(lastResult));

        AiAnalysisResponse response = service.requestAnalysis(encounterId, authId);

        verify(geminiClient, never()).analyze(any());
        verify(aiAnalysisResultDao, never()).save(any());
        assertThat(response.summary()).isEqualTo("Old summary.");
    }

    @Test
    void requestAnalysis_throws403_whenProviderNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestAnalysis(encounterId, authId))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void requestAnalysis_throws404_whenEncounterNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findById(encounterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.requestAnalysis(encounterId, authId))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void requestAnalysis_throws403_whenProviderNotOwner() {
        Encounter encounter = new Encounter(UUID.randomUUID(), OffsetDateTime.now());
        encounter.setId(encounterId);
        encounter.setPatientId(patientId);

        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findById(encounterId)).thenReturn(Optional.of(encounter));

        assertThatThrownBy(() -> service.requestAnalysis(encounterId, authId))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // -------------------------------------------------------------------------
    // getLatestAnalysisForPatient
    // -------------------------------------------------------------------------

    @Test
    void getLatestAnalysisForPatient_returnsResponse_whenResultExists() {
        AiAnalysisResult stored = new AiAnalysisResult(
                patientId, "Patient has Type 2 diabetes.", "[{\"flag\":\"High risk\",\"reason\":\"Diabetes\"}]",
                AiTriggerType.MANUAL, null, "gemini-1.5-pro", "{}", encounterId);

        when(aiAnalysisResultDao.findTopByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(Optional.of(stored));

        AiAnalysisResponse response = service.getLatestAnalysisForPatient(patientId, providerId);

        assertThat(response.patientId()).isEqualTo(patientId);
        assertThat(response.summary()).isEqualTo("Patient has Type 2 diabetes.");
        assertThat(response.triggerType()).isEqualTo(AiTriggerType.MANUAL);
    }

    @Test
    void getLatestAnalysisForPatient_throws404_whenNoResultExists() {
        when(aiAnalysisResultDao.findTopByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getLatestAnalysisForPatient(patientId, providerId))
                .isInstanceOf(AiServiceException.class)
                .satisfies(e -> assertThat(((AiServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getLatestAnalysisForPatient_writesAuditLog() {
        AiAnalysisResult stored = new AiAnalysisResult(
                patientId, "Summary", "[]",
                AiTriggerType.MANUAL, null, "gemini-1.5-pro", "{}", encounterId);

        when(aiAnalysisResultDao.findTopByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(Optional.of(stored));

        service.getLatestAnalysisForPatient(patientId, providerId);

        verify(auditLogDao).insert(any());
    }

    // -------------------------------------------------------------------------
    // getPatientHistory
    // -------------------------------------------------------------------------

    @Test
    void getPatientHistory_returnsList_whenResultsExist() {
        AiAnalysisResult r1 = new AiAnalysisResult(
                patientId, "Summary 1", "[]",
                AiTriggerType.MANUAL, null, "gemini-1.5-pro", "{}", encounterId);
        AiAnalysisResult r2 = new AiAnalysisResult(
                patientId, "Summary 2", "[]",
                AiTriggerType.MANUAL, null, "gemini-1.5-pro", "{}", encounterId);

        when(aiAnalysisResultDao.findByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(List.of(r1, r2));

        List<AiAnalysisResponse> history = service.getPatientHistory(patientId, providerId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).summary()).isEqualTo("Summary 1");
        assertThat(history.get(1).summary()).isEqualTo("Summary 2");
    }

    @Test
    void getPatientHistory_returnsEmptyList_whenNoResults() {
        when(aiAnalysisResultDao.findByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(List.of());

        List<AiAnalysisResponse> history = service.getPatientHistory(patientId, providerId);

        assertThat(history).isEmpty();
    }

    @Test
    void getPatientHistory_writesAuditLog() {
        when(aiAnalysisResultDao.findByPatientIdOrderByGeneratedAtDesc(patientId))
                .thenReturn(List.of());

        service.getPatientHistory(patientId, providerId);

        verify(auditLogDao).insert(any());
    }
}
