package com.healthcare.controller;

import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.dto.EncounterSummaryResponse;
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
 * Provider-facing appointment endpoints.
 * Auth enforced at gateway — X-User-Id is the provider's auth UUID.
 */
@RestController
@RequestMapping("/api/appointments/provider")
public class ProviderAppointmentController {

    private final AppointmentService appointmentService;

    public ProviderAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** GET /api/appointments/provider/encounters */
    @GetMapping("/encounters")
    public ResponseEntity<EncounterPageResponse> getEncounters(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "patient_id", required = false) UUID patientId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                appointmentService.getProviderEncounters(authId, from, to, patientId, page, size));
    }

    /** GET /api/appointments/provider/encounters/{id} */
    @GetMapping("/encounters/{id}")
    public ResponseEntity<EncounterDetailResponse> getEncounterDetail(
            @RequestHeader("X-User-Id") UUID authId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(appointmentService.getProviderEncounterDetail(authId, id));
    }

    /** GET /api/appointments/provider/patients/{id}/encounters */
    @GetMapping("/patients/{id}/encounters")
    public ResponseEntity<List<EncounterSummaryResponse>> getPatientEncounters(
            @RequestHeader("X-User-Id") UUID authId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(appointmentService.getPatientEncountersByProvider(authId, id));
    }
}
