package com.healthcare.service;

import com.healthcare.dao.AllergyDao;
import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.ConditionDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.EncounterResponse;
import com.healthcare.dto.PageResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.UpdatePatientRequest;
import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.exception.PatientServiceException;
import com.healthcare.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock private PatientDao   patientDao;
    @Mock private EncounterDao encounterDao;
    @Mock private ConditionDao conditionDao;
    @Mock private AllergyDao   allergyDao;
    @Mock private AuditLogDao  auditLogDao;

    private PatientServiceImpl service;

    private final UUID authId    = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();

    private Patient patient;

    @BeforeEach
    void setUp() {
        service = new PatientServiceImpl(patientDao, encounterDao, conditionDao, allergyDao, auditLogDao);
        patient = new Patient("MRN-000001", "John", "Doe");
    }

    // -------------------------------------------------------------------------
    // getProfile
    // -------------------------------------------------------------------------

    @Test
    void getProfile_returnsProfile_whenPatientExists() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));

        PatientProfileResponse response = service.getProfile(authId);

        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.mrn()).isEqualTo("MRN-000001");
        verify(patientDao).findByAuthId(authId);
    }

    @Test
    void getProfile_throws404_whenPatientNotFound() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(authId))
                .isInstanceOf(PatientServiceException.class)
                .satisfies(e -> assertThat(((PatientServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // updateProfile
    // -------------------------------------------------------------------------

    @Test
    void updateProfile_updatesFields_andReturnsUpdatedProfile() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(patientDao.save(any(Patient.class))).thenReturn(patient);

        UpdatePatientRequest req = new UpdatePatientRequest(
                "+15551234567", "Jane Doe - 206-555-0101",
                "123 Main St", "Seattle", "WA", "98101", "Updated notes");

        PatientProfileResponse response = service.updateProfile(authId, "john_doe", req);

        assertThat(response.firstName()).isEqualTo("John");
        verify(patientDao).save(patient);
    }

    @Test
    void updateProfile_skipsNullFields() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(patientDao.save(any(Patient.class))).thenReturn(patient);

        UpdatePatientRequest req = new UpdatePatientRequest(
                null, null, null, null, null, null, null);

        service.updateProfile(authId, "john_doe", req);

        verify(patientDao).save(patient);
    }

    @Test
    void updateProfile_throws404_whenPatientNotFound() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.empty());

        UpdatePatientRequest req = new UpdatePatientRequest(
                null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.updateProfile(authId, "john_doe", req))
                .isInstanceOf(PatientServiceException.class)
                .satisfies(e -> assertThat(((PatientServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // getEncounters
    // -------------------------------------------------------------------------

    @Test
    void getEncounters_returnsPaginatedEncounters() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        Encounter encounter = new Encounter(UUID.randomUUID(), OffsetDateTime.now());
        when(encounterDao.findByPatientId(any())).thenReturn(List.of(encounter));

        PageRequest pageable = PageRequest.of(0, 20, Sort.by("startTime").descending());
        PageResponse<EncounterResponse> response = service.getEncounters(authId, pageable);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.data()).hasSize(1);
    }

    @Test
    void getEncounters_returnsEmpty_whenNoEncounters() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(encounterDao.findByPatientId(any())).thenReturn(List.of());

        PageRequest pageable = PageRequest.of(0, 20, Sort.by("startTime").descending());
        PageResponse<EncounterResponse> response = service.getEncounters(authId, pageable);

        assertThat(response.total()).isEqualTo(0);
        assertThat(response.data()).isEmpty();
    }

    @Test
    void getEncounters_returnsEmptySlice_whenPageBeyondData() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        Encounter encounter = new Encounter(UUID.randomUUID(), OffsetDateTime.now());
        when(encounterDao.findByPatientId(any())).thenReturn(List.of(encounter));

        PageRequest pageable = PageRequest.of(5, 20, Sort.by("startTime").descending());
        PageResponse<EncounterResponse> response = service.getEncounters(authId, pageable);

        assertThat(response.total()).isEqualTo(1);
        assertThat(response.data()).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getConditions
    // -------------------------------------------------------------------------

    @Test
    void getConditions_returnsConditions() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        Condition condition = new Condition(
                new ConditionId(patientId, UUID.randomUUID(), "44054006"), LocalDate.now());
        when(conditionDao.findByIdPatientId(any())).thenReturn(List.of(condition));

        List<ConditionResponse> response = service.getConditions(authId);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).code()).isEqualTo("44054006");
    }

    @Test
    void getConditions_returnsEmpty_whenNone() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(conditionDao.findByIdPatientId(any())).thenReturn(List.of());

        assertThat(service.getConditions(authId)).isEmpty();
    }

    // -------------------------------------------------------------------------
    // getAllergies
    // -------------------------------------------------------------------------

    @Test
    void getAllergies_returnsAllergies() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        Allergy allergy = new Allergy(
                new AllergyId(patientId, UUID.randomUUID(), "111088007"), LocalDate.now());
        when(allergyDao.findByIdPatientId(any())).thenReturn(List.of(allergy));

        List<AllergyResponse> response = service.getAllergies(authId);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).code()).isEqualTo("111088007");
    }

    @Test
    void getAllergies_returnsEmpty_whenNone() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(allergyDao.findByIdPatientId(any())).thenReturn(List.of());

        assertThat(service.getAllergies(authId)).isEmpty();
    }
}
