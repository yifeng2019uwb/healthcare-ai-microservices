package com.healthcare.service;

import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.dto.EncounterSummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    EncounterPageResponse getPatientEncounters(
            UUID authId, LocalDate from, LocalDate to, String encounterClass, int page, int size);

    EncounterDetailResponse getPatientEncounterDetail(UUID authId, UUID encounterId);

    EncounterPageResponse getProviderEncounters(
            UUID authId, LocalDate from, LocalDate to, UUID patientId, int page, int size);

    EncounterDetailResponse getProviderEncounterDetail(UUID authId, UUID encounterId);

    List<EncounterSummaryResponse> getPatientEncountersByProvider(UUID authId, UUID patientId);
}
