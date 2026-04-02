package com.healthcare.service;

import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.EncounterResponse;
import com.healthcare.dto.PageResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.UpdatePatientRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Business logic contract for patient-service.
 *
 * All methods operate on the authenticated patient identified by authId
 * (the users.id propagated by the gateway in the X-User-Id header).
 */
public interface PatientService {

    PatientProfileResponse getProfile(UUID authId);

    PatientProfileResponse updateProfile(UUID authId, String username, UpdatePatientRequest request);

    PageResponse<EncounterResponse> getEncounters(UUID authId, Pageable pageable);

    List<ConditionResponse> getConditions(UUID authId);

    List<AllergyResponse> getAllergies(UUID authId);
}
