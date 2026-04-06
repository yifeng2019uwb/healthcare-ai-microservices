package com.healthcare.service.impl;

import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.dto.EncounterSummaryResponse;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.AppointmentServiceException;
import com.healthcare.service.AppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);

    private static final String RESOURCE_ENCOUNTERS = "encounters";

    private final PatientDao   patientDao;
    private final ProviderDao  providerDao;
    private final EncounterDao encounterDao;
    private final AuditLogDao  auditLogDao;

    public AppointmentServiceImpl(PatientDao patientDao,
                                  ProviderDao providerDao,
                                  EncounterDao encounterDao,
                                  AuditLogDao auditLogDao) {
        this.patientDao   = patientDao;
        this.providerDao  = providerDao;
        this.encounterDao = encounterDao;
        this.auditLogDao  = auditLogDao;
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterPageResponse getPatientEncounters(
            UUID authId, LocalDate from, LocalDate to, String encounterClass, int page, int size) {

        Patient patient = requirePatientByAuth(authId);

        List<Encounter> encounters = fetchPatientEncounters(patient.getId(), from, to);

        if (encounterClass != null && !encounterClass.isBlank()) {
            encounters = encounters.stream()
                    .filter(e -> encounterClass.equalsIgnoreCase(e.getEncounterClass()))
                    .collect(Collectors.toList());
        }

        encounters = sortByStartTimeDesc(encounters);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_ENCOUNTERS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PATIENT)
                .withResourceId(patient.getId()));

        return paginate(encounters, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterDetailResponse getPatientEncounterDetail(UUID authId, UUID encounterId) {
        Patient patient = requirePatientByAuth(authId);

        Encounter encounter = encounterDao.findById(encounterId)
                .orElseThrow(() -> new AppointmentServiceException(
                        HttpStatus.NOT_FOUND,
                        AppointmentServiceException.ENCOUNTER_NOT_FOUND,
                        "Encounter not found: " + encounterId));

        if (!Objects.equals(patient.getId(), encounter.getPatientId())) {
            log.warn("Patient {} attempted to access encounter {} belonging to another patient",
                    patient.getId(), encounterId);
            throw new AppointmentServiceException(
                    HttpStatus.FORBIDDEN,
                    AppointmentServiceException.ACCESS_DENIED,
                    "Access denied to encounter " + encounterId);
        }

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_ENCOUNTERS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PATIENT)
                .withResourceId(encounterId));

        return EncounterDetailResponse.from(encounter);
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterPageResponse getProviderEncounters(
            UUID authId, LocalDate from, LocalDate to, UUID patientId, int page, int size) {

        Provider provider = requireProviderByAuth(authId);

        List<Encounter> encounters = fetchProviderEncounters(provider.getId(), from, to);

        if (patientId != null) {
            encounters = encounters.stream()
                    .filter(e -> patientId.equals(e.getPatientId()))
                    .collect(Collectors.toList());
        }

        encounters = sortByStartTimeDesc(encounters);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_ENCOUNTERS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(provider.getId()));

        return paginate(encounters, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterDetailResponse getProviderEncounterDetail(UUID authId, UUID encounterId) {
        Provider provider = requireProviderByAuth(authId);

        Encounter encounter = encounterDao.findById(encounterId)
                .orElseThrow(() -> new AppointmentServiceException(
                        HttpStatus.NOT_FOUND,
                        AppointmentServiceException.ENCOUNTER_NOT_FOUND,
                        "Encounter not found: " + encounterId));

        if (!Objects.equals(provider.getId(), encounter.getProviderId())) {
            log.warn("Provider {} attempted to access encounter {} belonging to another provider",
                    provider.getId(), encounterId);
            throw new AppointmentServiceException(
                    HttpStatus.FORBIDDEN,
                    AppointmentServiceException.ACCESS_DENIED,
                    "Access denied to encounter " + encounterId);
        }

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_ENCOUNTERS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(encounterId));

        return EncounterDetailResponse.from(encounter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EncounterSummaryResponse> getPatientEncountersByProvider(UUID authId, UUID patientId) {
        Provider provider = requireProviderByAuth(authId);

        if (!encounterDao.existsByPatientIdAndProviderId(patientId, provider.getId())) {
            log.warn("Provider {} has no encounters with patient {}", provider.getId(), patientId);
            throw new AppointmentServiceException(
                    HttpStatus.FORBIDDEN,
                    AppointmentServiceException.ACCESS_DENIED,
                    "Provider has no encounters with patient " + patientId);
        }

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_ENCOUNTERS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patientId));

        return encounterDao.findByProviderIdAndPatientId(provider.getId(), patientId)
                .stream()
                .sorted((a, b) -> {
                    if (a.getStartTime() == null) return 1;
                    if (b.getStartTime() == null) return -1;
                    return b.getStartTime().compareTo(a.getStartTime());
                })
                .map(EncounterSummaryResponse::from)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Patient requirePatientByAuth(UUID authId) {
        return patientDao.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.warn("Patient not found for authId={}", authId);
                    return new AppointmentServiceException(
                            HttpStatus.NOT_FOUND,
                            AppointmentServiceException.PATIENT_NOT_FOUND,
                            "Patient not found for authId=" + authId);
                });
    }

    private Provider requireProviderByAuth(UUID authId) {
        return providerDao.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.warn("Provider not found for authId={}", authId);
                    return new AppointmentServiceException(
                            HttpStatus.NOT_FOUND,
                            AppointmentServiceException.PROVIDER_NOT_FOUND,
                            "Provider not found for authId=" + authId);
                });
    }

    private List<Encounter> fetchPatientEncounters(UUID patientId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return encounterDao.findByPatientIdAndStartTimeBetween(
                    patientId, toStartOfDay(from), toEndOfDay(to));
        }
        if (from != null) {
            return encounterDao.findByPatientIdAndStartTimeBetween(
                    patientId, toStartOfDay(from), OffsetDateTime.now(ZoneOffset.UTC).plusYears(100));
        }
        if (to != null) {
            return encounterDao.findByPatientIdAndStartTimeBetween(
                    patientId, OffsetDateTime.MIN, toEndOfDay(to));
        }
        return encounterDao.findByPatientId(patientId);
    }

    private List<Encounter> fetchProviderEncounters(UUID providerId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return encounterDao.findByProviderIdAndStartTimeBetween(
                    providerId, toStartOfDay(from), toEndOfDay(to));
        }
        if (from != null) {
            return encounterDao.findByProviderIdAndStartTimeBetween(
                    providerId, toStartOfDay(from), OffsetDateTime.now(ZoneOffset.UTC).plusYears(100));
        }
        if (to != null) {
            return encounterDao.findByProviderIdAndStartTimeBetween(
                    providerId, OffsetDateTime.MIN, toEndOfDay(to));
        }
        return encounterDao.findByProviderId(providerId);
    }

    private List<Encounter> sortByStartTimeDesc(List<Encounter> encounters) {
        return encounters.stream()
                .sorted((a, b) -> {
                    if (a.getStartTime() == null) return 1;
                    if (b.getStartTime() == null) return -1;
                    return b.getStartTime().compareTo(a.getStartTime());
                })
                .collect(Collectors.toList());
    }

    private EncounterPageResponse paginate(List<Encounter> all, int page, int size) {
        long total = all.size();
        int start  = (page - 1) * size;   // page is 1-based per design doc
        int end    = Math.min(start + size, all.size());

        List<EncounterSummaryResponse> data = (start >= all.size())
                ? List.of()
                : all.subList(start, end).stream()
                        .map(EncounterSummaryResponse::from)
                        .collect(Collectors.toList());

        return new EncounterPageResponse(total, page, size, data);
    }

    private static OffsetDateTime toStartOfDay(LocalDate date) {
        return date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    private static OffsetDateTime toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);
    }
}
