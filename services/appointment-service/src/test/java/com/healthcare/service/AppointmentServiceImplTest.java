package com.healthcare.service;

import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.dto.EncounterSummaryResponse;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.exception.AppointmentServiceException;
import com.healthcare.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private PatientDao   patientDao;
    @Mock private ProviderDao  providerDao;
    @Mock private EncounterDao encounterDao;
    @Mock private AuditLogDao  auditLogDao;

    private AppointmentServiceImpl service;

    private final UUID authId      = UUID.randomUUID();
    private final UUID patientId   = UUID.randomUUID();
    private final UUID providerId  = UUID.randomUUID();
    private final UUID encounterId = UUID.randomUUID();

    private Patient  patient;
    private Provider mockProvider;

    @BeforeEach
    void setUp() {
        service = new AppointmentServiceImpl(patientDao, providerDao, encounterDao, auditLogDao);

        patient = new Patient("MRN-000001", "John", "Doe");

        mockProvider = mock(Provider.class);
        lenient().when(mockProvider.getId()).thenReturn(providerId);
    }

    private Encounter encounter() {
        Encounter e = new Encounter(providerId, OffsetDateTime.now());
        e.setPatientId(patientId);
        return e;
    }

    // -------------------------------------------------------------------------
    // getPatientEncounters
    // -------------------------------------------------------------------------

    @Test
    void getPatientEncounters_returnsPage_whenPatientExists() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(encounterDao.findByPatientId(any())).thenReturn(List.of(encounter()));

        EncounterPageResponse page = service.getPatientEncounters(authId, null, null, null, 1, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.page()).isEqualTo(1);
        assertThat(page.encounters()).hasSize(1);
    }

    @Test
    void getPatientEncounters_filtersBy_encounterClass() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        Encounter amb = encounter();
        amb.setEncounterClass("ambulatory");
        Encounter emer = encounter();
        emer.setEncounterClass("emergency");
        when(encounterDao.findByPatientId(any())).thenReturn(List.of(amb, emer));

        EncounterPageResponse page = service.getPatientEncounters(authId, null, null, "ambulatory", 1, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.encounters().get(0).encounterClass()).isEqualTo("ambulatory");
    }

    @Test
    void getPatientEncounters_returnsEmpty_whenPageBeyondData() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(encounterDao.findByPatientId(any())).thenReturn(List.of(encounter()));

        EncounterPageResponse page = service.getPatientEncounters(authId, null, null, null, 5, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.encounters()).isEmpty();
    }

    @Test
    void getPatientEncounters_throws404_whenPatientNotFound() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPatientEncounters(authId, null, null, null, 1, 10))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // getPatientEncounterDetail
    // -------------------------------------------------------------------------

    @Test
    void getPatientEncounterDetail_returnsDetail_whenOwned() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        // patient.getId() == null (no DB) — create encounter with patientId=null so they match
        Encounter enc = new Encounter(providerId, OffsetDateTime.now()); // patientId not set → null
        when(encounterDao.findById(encounterId)).thenReturn(Optional.of(enc));

        EncounterDetailResponse detail = service.getPatientEncounterDetail(authId, encounterId);
        assertThat(detail).isNotNull();
    }

    @Test
    void getPatientEncounterDetail_throws404_whenEncounterNotFound() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        when(encounterDao.findById(encounterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPatientEncounterDetail(authId, encounterId))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getPatientEncounterDetail_throws403_whenEncounterBelongsToOtherPatient() {
        when(patientDao.findByAuthId(authId)).thenReturn(Optional.of(patient));
        Encounter enc = new Encounter(providerId, OffsetDateTime.now());
        enc.setPatientId(UUID.randomUUID()); // different patient
        when(encounterDao.findById(encounterId)).thenReturn(Optional.of(enc));

        assertThatThrownBy(() -> service.getPatientEncounterDetail(authId, encounterId))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // -------------------------------------------------------------------------
    // getProviderEncounters
    // -------------------------------------------------------------------------

    @Test
    void getProviderEncounters_returnsPage_whenProviderExists() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findByProviderId(providerId)).thenReturn(List.of(encounter()));

        EncounterPageResponse page = service.getProviderEncounters(authId, null, null, null, 1, 10);

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.encounters()).hasSize(1);
    }

    @Test
    void getProviderEncounters_filtersBy_patientId() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        Encounter enc1 = encounter(); // enc1.patientId = patientId
        Encounter enc2 = new Encounter(providerId, OffsetDateTime.now());
        enc2.setPatientId(UUID.randomUUID()); // different patient
        when(encounterDao.findByProviderId(providerId)).thenReturn(List.of(enc1, enc2));

        EncounterPageResponse page = service.getProviderEncounters(authId, null, null, patientId, 1, 10);

        assertThat(page.total()).isEqualTo(1);
    }

    @Test
    void getProviderEncounters_throws404_whenProviderNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProviderEncounters(authId, null, null, null, 1, 10))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // -------------------------------------------------------------------------
    // getProviderEncounterDetail
    // -------------------------------------------------------------------------

    @Test
    void getProviderEncounterDetail_throws404_whenEncounterNotFound() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.findById(encounterId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProviderEncounterDetail(authId, encounterId))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getProviderEncounterDetail_throws403_whenEncounterBelongsToOtherProvider() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        Encounter enc = new Encounter(UUID.randomUUID(), OffsetDateTime.now()); // different provider
        when(encounterDao.findById(encounterId)).thenReturn(Optional.of(enc));

        assertThatThrownBy(() -> service.getProviderEncounterDetail(authId, encounterId))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }

    // -------------------------------------------------------------------------
    // getPatientEncountersByProvider
    // -------------------------------------------------------------------------

    @Test
    void getPatientEncountersByProvider_returnsList_whenAccessGranted() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.existsByPatientIdAndProviderId(patientId, providerId)).thenReturn(true);
        when(encounterDao.findByProviderIdAndPatientId(providerId, patientId)).thenReturn(List.of(encounter()));

        List<EncounterSummaryResponse> result = service.getPatientEncountersByProvider(authId, patientId);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPatientEncountersByProvider_throws403_whenNoAccess() {
        when(providerDao.findByAuthId(authId)).thenReturn(Optional.of(mockProvider));
        when(encounterDao.existsByPatientIdAndProviderId(patientId, providerId)).thenReturn(false);

        assertThatThrownBy(() -> service.getPatientEncountersByProvider(authId, patientId))
                .isInstanceOf(AppointmentServiceException.class)
                .satisfies(e -> assertThat(((AppointmentServiceException) e).getStatus())
                        .isEqualTo(HttpStatus.FORBIDDEN));
    }
}
