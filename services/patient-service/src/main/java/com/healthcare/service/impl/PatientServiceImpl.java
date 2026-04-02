package com.healthcare.service.impl;

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
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Patient;
import com.healthcare.enums.ActionType;
import com.healthcare.enums.Outcome;
import com.healthcare.enums.UserRole;
import com.healthcare.exception.PatientServiceException;
import com.healthcare.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    private static final String RESOURCE_PATIENTS = "patients";

    private final PatientDao patientDao;
    private final EncounterDao encounterDao;
    private final ConditionDao conditionDao;
    private final AllergyDao allergyDao;
    private final AuditLogDao auditLogDao;

    public PatientServiceImpl(PatientDao patientDao,
                              EncounterDao encounterDao,
                              ConditionDao conditionDao,
                              AllergyDao allergyDao,
                              AuditLogDao auditLogDao) {
        this.patientDao   = patientDao;
        this.encounterDao = encounterDao;
        this.conditionDao = conditionDao;
        this.allergyDao   = allergyDao;
        this.auditLogDao  = auditLogDao;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getProfile(UUID authId) {
        Patient patient = requirePatient(authId);

        auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_PATIENTS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PATIENT)
                .withResourceId(patient.getId()));

        return PatientProfileResponse.from(patient);
    }

    @Override
    @Transactional
    public PatientProfileResponse updateProfile(UUID authId, String username, UpdatePatientRequest req) {
        Patient patient = requirePatient(authId);

        if (req.phone() != null)            patient.setPhone(req.phone());
        if (req.emergencyContact() != null) patient.setEmergencyContact(req.emergencyContact());
        if (req.address() != null)          patient.setAddress(req.address());
        if (req.city() != null)             patient.setCity(req.city());
        if (req.state() != null)            patient.setState(req.state());
        if (req.zip() != null)              patient.setZip(req.zip());
        if (req.notes() != null)            patient.setNotes(req.notes());

        patient.setUpdatedBy(username);
        patient = patientDao.save(patient);

        auditLogDao.insert(new AuditLog(ActionType.UPDATE, RESOURCE_PATIENTS, Outcome.SUCCESS)
                .withAuthId(authId.toString())
                .withUserRole(UserRole.PATIENT)
                .withResourceId(patient.getId()));

        return PatientProfileResponse.from(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncounterResponse> getEncounters(UUID authId, Pageable pageable) {
        Patient patient = requirePatient(authId);

        List<EncounterResponse> all = encounterDao.findByPatientId(patient.getId())
                .stream()
                .map(EncounterResponse::from)
                .collect(Collectors.toList());

        int start  = (int) pageable.getOffset();
        int end    = Math.min(start + pageable.getPageSize(), all.size());
        List<EncounterResponse> slice = (start >= all.size()) ? List.of() : all.subList(start, end);

        Page<EncounterResponse> page = new PageImpl<>(slice, pageable, all.size());
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConditionResponse> getConditions(UUID authId) {
        Patient patient = requirePatient(authId);
        return conditionDao.findByIdPatientId(patient.getId())
                .stream()
                .map(ConditionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllergyResponse> getAllergies(UUID authId) {
        Patient patient = requirePatient(authId);
        return allergyDao.findByIdPatientId(patient.getId())
                .stream()
                .map(AllergyResponse::from)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------

    private Patient requirePatient(UUID authId) {
        return patientDao.findByAuthId(authId)
                .orElseThrow(() -> {
                    log.warn("Patient not found for authId={}", authId);
                    auditLogDao.insert(new AuditLog(ActionType.READ, RESOURCE_PATIENTS, Outcome.FAILURE)
                            .withAuthId(authId.toString())
                            .withUserRole(UserRole.PATIENT));
                    return new PatientServiceException(
                            HttpStatus.NOT_FOUND,
                            PatientServiceException.PATIENT_NOT_FOUND,
                            "Patient not found for authId=" + authId);
                });
    }
}
