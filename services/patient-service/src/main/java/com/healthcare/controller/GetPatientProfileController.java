package com.healthcare.controller;

import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.EncounterResponse;
import com.healthcare.dto.PageResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.UpdatePatientRequest;
import com.healthcare.fhir.FhirMapper;
import com.healthcare.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles all authenticated patient self-service endpoints.
 *
 * Auth is enforced at the gateway — every request here already carries
 * X-User-Id / X-Username / X-User-Role injected by the gateway JWT filter.
 */
@RestController
@RequestMapping("/api/patients/me")
public class GetPatientProfileController {

    private final PatientService patientService;

    public GetPatientProfileController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** GET /api/patients/me — JSON */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PatientProfileResponse> getProfile(
            @RequestHeader("X-User-Id") UUID authId) {
        return ResponseEntity.ok(patientService.getProfile(authId));
    }

    /** GET /api/patients/me — FHIR R4 Patient resource */
    @GetMapping(produces = "application/fhir+json")
    public ResponseEntity<Map<String, Object>> getProfileFhir(
            @RequestHeader("X-User-Id") UUID authId) {
        PatientProfileResponse profile = patientService.getProfile(authId);
        return ResponseEntity.ok(FhirMapper.toPatient(profile));
    }

    /** PUT /api/patients/me */
    @PutMapping
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody UpdatePatientRequest request) {
        return ResponseEntity.ok(patientService.updateProfile(authId, username, request));
    }

    /** GET /api/patients/me/encounters?page=0&size=20 — JSON */
    @GetMapping(value = "/encounters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageResponse<EncounterResponse>> getEncounters(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return ResponseEntity.ok(patientService.getEncounters(authId, pageable));
    }

    /** GET /api/patients/me/encounters — FHIR R4 Bundle of Encounter resources */
    @GetMapping(value = "/encounters", produces = "application/fhir+json")
    public ResponseEntity<Map<String, Object>> getEncountersFhir(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PatientProfileResponse profile = patientService.getProfile(authId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        PageResponse<EncounterResponse> encounters = patientService.getEncounters(authId, pageable);
        return ResponseEntity.ok(FhirMapper.toEncounterBundle(profile.id(), encounters));
    }

    /** GET /api/patients/me/conditions — JSON */
    @GetMapping(value = "/conditions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ConditionResponse>> getConditions(
            @RequestHeader("X-User-Id") UUID authId) {
        return ResponseEntity.ok(patientService.getConditions(authId));
    }

    /** GET /api/patients/me/conditions — FHIR R4 Bundle of Condition resources */
    @GetMapping(value = "/conditions", produces = "application/fhir+json")
    public ResponseEntity<Map<String, Object>> getConditionsFhir(
            @RequestHeader("X-User-Id") UUID authId) {
        PatientProfileResponse profile = patientService.getProfile(authId);
        List<ConditionResponse> conditions = patientService.getConditions(authId);
        return ResponseEntity.ok(FhirMapper.toConditionBundle(profile.id(), conditions));
    }

    /** GET /api/patients/me/allergies — JSON */
    @GetMapping(value = "/allergies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AllergyResponse>> getAllergies(
            @RequestHeader("X-User-Id") UUID authId) {
        return ResponseEntity.ok(patientService.getAllergies(authId));
    }

    /** GET /api/patients/me/allergies — FHIR R4 Bundle of AllergyIntolerance resources */
    @GetMapping(value = "/allergies", produces = "application/fhir+json")
    public ResponseEntity<Map<String, Object>> getAllergiesFhir(
            @RequestHeader("X-User-Id") UUID authId) {
        PatientProfileResponse profile = patientService.getProfile(authId);
        List<AllergyResponse> allergies = patientService.getAllergies(authId);
        return ResponseEntity.ok(FhirMapper.toAllergyBundle(profile.id(), allergies));
    }
}
