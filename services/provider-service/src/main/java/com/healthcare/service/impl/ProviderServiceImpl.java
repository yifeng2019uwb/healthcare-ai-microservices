package com.healthcare.service.impl;

import com.healthcare.dao.AllergyDao;
import com.healthcare.dao.AuditLogDao;
import com.healthcare.dao.ConditionDao;
import com.healthcare.dao.EncounterDao;
import com.healthcare.dao.PatientDao;
import com.healthcare.dao.ProviderDao;
import com.healthcare.dto.AddAllergyRequest;
import com.healthcare.dto.AddConditionRequest;
import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.PatientSummaryResponse;
import com.healthcare.dto.ProviderProfileResponse;
import com.healthcare.entity.Allergy;
import com.healthcare.entity.AllergyId;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Condition;
import com.healthcare.entity.ConditionId;
import com.healthcare.entity.Encounter;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Provider;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.ProviderServiceException;
import com.healthcare.service.ProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProviderServiceImpl implements ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderServiceImpl.class);

    private static final String RESOURCE_PROVIDERS  = "providers";
    private static final String RESOURCE_PATIENTS   = "patients";
    private static final String RESOURCE_CONDITIONS = "conditions";
    private static final String RESOURCE_ALLERGIES  = "allergies";

    private final ProviderDao  providerDao;
    private final PatientDao   patientDao;
    private final EncounterDao encounterDao;
    private final ConditionDao conditionDao;
    private final AllergyDao   allergyDao;
    private final AuditLogDao  auditLogDao;

    public ProviderServiceImpl(ProviderDao providerDao,
                               PatientDao patientDao,
                               EncounterDao encounterDao,
                               ConditionDao conditionDao,
                               AllergyDao allergyDao,
                               AuditLogDao auditLogDao) {
        this.providerDao  = providerDao;
        this.patientDao   = patientDao;
        this.encounterDao = encounterDao;
        this.conditionDao = conditionDao;
        this.allergyDao   = allergyDao;
        this.auditLogDao  = auditLogDao;
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderProfileResponse getProfile(UUID authId) {
        Provider provider = requireProvider(authId);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_PROVIDERS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(provider.getId()));

        return ProviderProfileResponse.from(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryResponse> getPatients(UUID authId, Pageable pageable) {
        Provider provider = requireProvider(authId);

        List<Encounter> encounters = encounterDao.findByProviderId(provider.getId());

        // Collect unique patient IDs with their latest encounter time
        Map<UUID, Encounter> latestByPatient = encounters.stream()
                .collect(Collectors.toMap(
                        Encounter::getPatientId,
                        e -> e,
                        (a, b) -> a.getStartTime() != null && b.getStartTime() != null
                                && a.getStartTime().isAfter(b.getStartTime()) ? a : b));

        List<PatientSummaryResponse> result = latestByPatient.entrySet().stream()
                .map(entry -> {
                    UUID patientId  = entry.getKey();
                    Encounter latest = entry.getValue();
                    return patientDao.findById(patientId)
                            .map(p -> PatientSummaryResponse.from(p, latest.getStartTime()))
                            .orElse(null);
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(PatientSummaryResponse::lastEncounterDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_PATIENTS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(provider.getId()));

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), result.size());
        return start >= result.size() ? List.of() : result.subList(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatient(UUID authId, UUID patientId) {
        Provider provider = requireProvider(authId);
        Patient  patient  = requirePatient(patientId);
        requireEncounterAccess(patient.getId(), provider.getId(), authId);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_PATIENTS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patient.getId()));

        return PatientProfileResponse.from(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionResponse> getPatientConditions(UUID authId, UUID patientId) {
        Provider provider = requireProvider(authId);
        Patient  patient  = requirePatient(patientId);
        requireEncounterAccess(patient.getId(), provider.getId(), authId);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_CONDITIONS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patient.getId()));

        return conditionDao.findByIdPatientId(patient.getId())
                .stream()
                .map(ConditionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllergyResponse> getPatientAllergies(UUID authId, UUID patientId) {
        Provider provider = requireProvider(authId);
        Patient  patient  = requirePatient(patientId);
        requireEncounterAccess(patient.getId(), provider.getId(), authId);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_ALLERGIES, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(patient.getId()));

        return allergyDao.findByIdPatientId(patient.getId())
                .stream()
                .map(AllergyResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConditionResponse addCondition(UUID authId, UUID encounterId, AddConditionRequest request) {
        Provider provider = requireProvider(authId);

        Encounter encounter = encounterDao.findById(encounterId)
                .orElseThrow(() -> new ProviderServiceException(
                        HttpStatus.NOT_FOUND,
                        ProviderServiceException.ENCOUNTER_NOT_FOUND,
                        "Encounter not found: " + encounterId));

        if (!provider.getId().equals(encounter.getProviderId())) {
            throw new ProviderServiceException(
                    HttpStatus.FORBIDDEN,
                    ProviderServiceException.ACCESS_DENIED,
                    "Provider not associated with encounter: " + encounterId);
        }

        ConditionId id = new ConditionId(encounter.getPatientId(), encounterId, request.code());
        Condition condition = new Condition(id, request.startDate());
        condition.setDescription(request.description());
        condition.setStopDate(request.stopDate());
        conditionDao.save(condition);

        auditLogDao.insert(new AuditLog(ActionType.CREATE, RESOURCE_CONDITIONS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(encounter.getPatientId()));

        return ConditionResponse.from(condition);
    }

    @Override
    @Transactional
    public AllergyResponse addAllergy(UUID authId, UUID encounterId, AddAllergyRequest request) {
        Provider provider = requireProvider(authId);

        Encounter encounter = encounterDao.findById(encounterId)
                .orElseThrow(() -> new ProviderServiceException(
                        HttpStatus.NOT_FOUND,
                        ProviderServiceException.ENCOUNTER_NOT_FOUND,
                        "Encounter not found: " + encounterId));

        if (!provider.getId().equals(encounter.getProviderId())) {
            throw new ProviderServiceException(
                    HttpStatus.FORBIDDEN,
                    ProviderServiceException.ACCESS_DENIED,
                    "Provider not associated with encounter: " + encounterId);
        }

        AllergyId id = new AllergyId(encounter.getPatientId(), encounterId, request.code());
        Allergy allergy = new Allergy(id, request.startDate());
        allergy.setDescription(request.description());
        allergy.setStopDate(request.stopDate());
        allergy.setAllergyType(request.allergyType());
        allergy.setCategory(request.category());
        allergy.setReaction1(request.reaction1());
        allergy.setDescription1(request.description1());
        allergy.setSeverity1(request.severity1());
        allergy.setReaction2(request.reaction2());
        allergy.setDescription2(request.description2());
        allergy.setSeverity2(request.severity2());
        allergyDao.save(allergy);

        auditLogDao.insert(new AuditLog(ActionType.CREATE, RESOURCE_ALLERGIES, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PROVIDER)
                .withResourceId(encounter.getPatientId()));

        return AllergyResponse.from(allergy);
    }

    // -------------------------------------------------------------------------

    private Provider requireProvider(UUID authId) {
        return providerDao.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.warn("Provider not found for authId={}", authId);
                    return new ProviderServiceException(
                            HttpStatus.NOT_FOUND,
                            ProviderServiceException.PROVIDER_NOT_FOUND,
                            "Provider not found for authId=" + authId);
                });
    }

    private Patient requirePatient(UUID patientId) {
        return patientDao.findById(patientId)
                .orElseThrow(() -> new ProviderServiceException(
                        HttpStatus.NOT_FOUND,
                        ProviderServiceException.PATIENT_NOT_FOUND,
                        "Patient not found: " + patientId));
    }

    private void requireEncounterAccess(UUID patientId, UUID providerId, UUID authId) {
        if (!encounterDao.existsByPatientIdAndProviderId(patientId, providerId)) {
            log.warn("Provider authId={} has no encounters with patient={}", authId, patientId);
            throw new ProviderServiceException(
                    HttpStatus.FORBIDDEN,
                    ProviderServiceException.ACCESS_DENIED,
                    "Provider has no encounters with patient " + patientId);
        }
    }
}
