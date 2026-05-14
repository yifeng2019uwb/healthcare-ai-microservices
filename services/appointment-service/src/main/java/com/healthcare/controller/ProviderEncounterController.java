package com.healthcare.controller;

import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.dto.EncounterSummaryResponse;
import com.healthcare.constants.SecurityConstants;
import com.healthcare.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Provider-facing encounter history endpoints.
 * Auth enforced at gateway — X-User-Id is the provider's auth UUID.
 *
 * Path: /api/encounters/provider
 */
@RestController
@RequestMapping("/api/encounters/provider")
public class ProviderEncounterController {

    private final AppointmentService appointmentService;

    public ProviderEncounterController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** GET /api/encounters/provider */
    @GetMapping
    public ResponseEntity<EncounterPageResponse> getEncounters(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) UUID authId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "patient_id", required = false) UUID patientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                appointmentService.getProviderEncounters(authId, from, to, patientId, page, size));
    }

    /** GET /api/encounters/provider/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<EncounterDetailResponse> getEncounterDetail(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) UUID authId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(appointmentService.getProviderEncounterDetail(authId, id));
    }

    /** GET /api/encounters/provider/patients/{patientId} */
    @GetMapping("/patients/{patientId}")
    public ResponseEntity<List<EncounterSummaryResponse>> getPatientEncounters(
            @RequestHeader(SecurityConstants.HEADER_USER_ID) UUID authId,
            @PathVariable UUID patientId) {

        return ResponseEntity.ok(appointmentService.getPatientEncountersByProvider(authId, patientId));
    }
}
