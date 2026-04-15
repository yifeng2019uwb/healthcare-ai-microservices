package com.healthcare.service;

import com.healthcare.dao.AllergyDao;
import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.ConditionDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.PatientSummaryResponse;
import com.healthcare.dto.ProviderProfileResponse;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.dto.RegisterPatientResponse;
import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.exception.ProviderServiceException;
import com.healthcare.service.impl.ProviderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceImplTest {

    @Mock private ProviderDao  providerDao;
    @Mock private PatientDao   patientDao;
    @Mock private EncounterDao encounterDao;
    @Mock private ConditionDao conditionDao;
    @Mock private AllergyDao   allergyDao;
    @Mock private AuditLogDao  auditLogDao;

    @InjectMocks
    private ProviderServiceImpl service;

    private final UUID authId    = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();

    private Provider mockProvider;
    private Patient  patient;

    @BeforeEach
    void setUp() {
        mockProvider = mock(Provider.class);
        lenient().when(mockProvider.getId()).thenReturn(UUID.randomUUID());
        lenient().when(mockProvider.getProviderCode()).thenReturn("PRV-000001");

        patient = new Patient("MRN-000001", "John", "Doe");
    }

    // -------------------------------------------------------------------------
    // getProfile
    // -------------------------------------------------------------------------

    @Test
    void getProfile_returnsProfile_whenProviderExists() {
        when(mockProvider.getName()).thenReturn("Dr. Smith");
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));

        ProviderProfileResponse response = service.getProfile(authId);

        assertThat(response.name()).isEqualTo("Dr. Smith");
        assertThat(response.providerCode()).isEqualTo("PRV-000001");
        verify(providerDao).findByAuthId(authId);
    }

    @Test
    void getProfile_throws404_whenProviderNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(authId))
                .isInstanceOf(ProviderServiceException.class)
                .satisfies(e -> assertThat(((ProviderServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // registerPatient
    // -------------------------------------------------------------------------

    @Test
    void registerPatient_createsPatient_andReturnsResponse() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.saveAndFlush(any(Patient.class))).thenReturn(patient);

        RegisterPatientRequest req = new RegisterPatientRequest(
                "John", null, "Doe", LocalDate.of(1990, 1, 15),
                "M", null, null, null, null, null, null, null, null, null, null);

        RegisterPatientResponse response = service.registerPatient(authId, "dr_smith", req);

        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.mrn()).isEqualTo("MRN-000001");
        verify(patientDao).saveAndFlush(any(Patient.class));
    }

    @Test
    void registerPatient_throws404_whenProviderNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.empty());

        RegisterPatientRequest req = new RegisterPatientRequest(
                "John", null, "Doe", null, null, null, null,
                null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.registerPatient(authId, "dr_smith", req))
                .isInstanceOf(ProviderServiceException.class)
                .satisfies(e -> assertThat(((ProviderServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // getPatients
    // -------------------------------------------------------------------------

    @Test
    void getPatients_returnsPatientsSortedByLatestEncounter() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));

        Encounter encounter = new Encounter(providerId, OffsetDateTime.now());
        encounter.setPatientId(patientId);
        when(encounterDao.findByProviderId(providerId)).thenReturn(List.of(encounter));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));

        List<PatientSummaryResponse> result = service.getPatients(authId, PageRequest.of(0, 20));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).mrn()).isEqualTo("MRN-000001");
    }

    @Test
    void getPatients_returnsEmpty_whenNoEncounters() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findByProviderId(any())).thenReturn(List.of());

        List<PatientSummaryResponse> result = service.getPatients(authId, PageRequest.of(0, 20));

        assertThat(result).isEmpty();
    }

    @Test
    void getPatients_returnsEmpty_whenPageBeyondData() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));

        Encounter encounter = new Encounter(providerId, OffsetDateTime.now());
        encounter.setPatientId(patientId);
        when(encounterDao.findByProviderId(providerId)).thenReturn(List.of(encounter));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));

        List<PatientSummaryResponse> result = service.getPatients(authId, PageRequest.of(5, 20));

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getPatient
    // -------------------------------------------------------------------------

    @Test
    void getPatient_returnsPatientProfile_whenAccessGranted() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(true);

        PatientProfileResponse response = service.getPatient(authId, patientId);

        assertThat(response.mrn()).isEqualTo("MRN-000001");
        assertThat(response.firstName()).isEqualTo("John");
    }

    @Test
    void getPatient_throws403_whenNoEncounterAccess() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(false);

        assertThatThrownBy(() -> service.getPatient(authId, patientId))
                .isInstanceOf(ProviderServiceException.class)
                .satisfies(e -> assertThat(((ProviderServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void getPatient_throws404_whenPatientNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPatient(authId, patientId))
                .isInstanceOf(ProviderServiceException.class)
                .satisfies(e -> assertThat(((ProviderServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // getPatientConditions
    // -------------------------------------------------------------------------

    @Test
    void getPatientConditions_returnsConditions_whenAccessGranted() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(true);

        Condition condition = new Condition(
                new ConditionId(patientId, UUID.randomUUID(), "44054006"), LocalDate.now());
        when(conditionDao.findByIdPatientId(any())).thenReturn(List.of(condition));

        List<ConditionResponse> result = service.getPatientConditions(authId, patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("44054006");
    }

    @Test
    void getPatientConditions_returnsEmpty_whenNone() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(true);
        when(conditionDao.findByIdPatientId(any())).thenReturn(List.of());

        assertThat(service.getPatientConditions(authId, patientId)).isEmpty();
    }

    @Test
    void getPatientConditions_throws403_whenNoAccess() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(false);

        assertThatThrownBy(() -> service.getPatientConditions(authId, patientId))
                .isInstanceOf(ProviderServiceException.class)
                .satisfies(e -> assertThat(((ProviderServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // -------------------------------------------------------------------------
    // getPatientAllergies
    // -------------------------------------------------------------------------

    @Test
    void getPatientAllergies_returnsAllergies_whenAccessGranted() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(true);

        Allergy allergy = new Allergy(
                new AllergyId(patientId, UUID.randomUUID(), "111088007"), LocalDate.now());
        when(allergyDao.findByIdPatientId(any())).thenReturn(List.of(allergy));

        List<AllergyResponse> result = service.getPatientAllergies(authId, patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("111088007");
    }

    @Test
    void getPatientAllergies_returnsEmpty_whenNone() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(true);
        when(allergyDao.findByIdPatientId(any())).thenReturn(List.of());

        assertThat(service.getPatientAllergies(authId, patientId)).isEmpty();
    }

    @Test
    void getPatientAllergies_throws403_whenNoAccess() {
        UUID providerId = mockProvider.getId();
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(patientDao.findById(patientId)).thenReturn(Optional.of(patient));
        when(encounterDao.existsByPatientIdAndProviderId(any(), eq(providerId))).thenReturn(false);

        assertThatThrownBy(() -> service.getPatientAllergies(authId, patientId))
                .isInstanceOf(ProviderServiceException.class)
                .satisfies(e -> assertThat(((ProviderServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }
}
