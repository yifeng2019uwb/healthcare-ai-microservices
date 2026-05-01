package com.healthcare.service;

import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.PatientSummaryResponse;
import com.healthcare.dto.ProviderProfileResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProviderService {

    ProviderProfileResponse getProfile(UUID authId);

    List<PatientSummaryResponse> getPatients(UUID authId, Pageable pageable);

    PatientProfileResponse getPatient(UUID authId, UUID patientId);

    List<ConditionResponse> getPatientConditions(UUID authId, UUID patientId);

    List<AllergyResponse> getPatientAllergies(UUID authId, UUID patientId);
}
