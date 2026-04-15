package com.healthcare.controller;

import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.PatientSummaryResponse;
import com.healthcare.dto.ProviderProfileResponse;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.dto.RegisterPatientResponse;
import com.healthcare.service.ProviderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Provider self-service endpoints.
 *
 * Auth enforced at gateway — every request carries X-User-Id / X-Username / X-User-Role.
 */
@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    /** GET /api/provider/me */
    @GetMapping("/me")
    public ResponseEntity<ProviderProfileResponse> getProfile(
            @RequestHeader("X-User-Id") UUID authId) {
        return ResponseEntity.ok(providerService.getProfile(authId));
    }

    /** POST /api/provider/patients/onboard */
    @PostMapping("/patients/onboard")
    public ResponseEntity<RegisterPatientResponse> onboardPatient(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestHeader("X-Username") String username,
            @Valid @RequestBody RegisterPatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(providerService.onboardPatient(authId, username, request));
    }

    /** GET /api/provider/patients?page=0&size=20 */
    @GetMapping("/patients")
    public ResponseEntity<List<PatientSummaryResponse>> getPatients(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.unsorted());
        return ResponseEntity.ok(providerService.getPatients(authId, pageable));
    }

    /** GET /api/provider/patients/{id} */
    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientProfileResponse> getPatient(
            @RequestHeader("X-User-Id") UUID authId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getPatient(authId, id));
    }

    /** GET /api/provider/patients/{id}/conditions */
    @GetMapping("/patients/{id}/conditions")
    public ResponseEntity<List<ConditionResponse>> getPatientConditions(
            @RequestHeader("X-User-Id") UUID authId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getPatientConditions(authId, id));
    }

    /** GET /api/provider/patients/{id}/allergies */
    @GetMapping("/patients/{id}/allergies")
    public ResponseEntity<List<AllergyResponse>> getPatientAllergies(
            @RequestHeader("X-User-Id") UUID authId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(providerService.getPatientAllergies(authId, id));
    }
}
