package com.healthcare.controller;

import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
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
import java.util.UUID;

/**
 * Patient-facing encounter history endpoints.
 * Auth enforced at gateway — X-User-Id is the patient's auth UUID.
 *
 * Path: /api/encounters/me
 */
@RestController
@RequestMapping("/api/encounters/me")
public class PatientEncounterController {

    private final AppointmentService appointmentService;

    public PatientEncounterController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** GET /api/encounters/me */
    @GetMapping
    public ResponseEntity<EncounterPageResponse> getEncounters(
            @RequestHeader("X-User-Id") UUID authId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "class", required = false) String encounterClass,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                appointmentService.getPatientEncounters(authId, from, to, encounterClass, page, size));
    }

    /** GET /api/encounters/me/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<EncounterDetailResponse> getEncounterDetail(
            @RequestHeader("X-User-Id") UUID authId,
            @PathVariable UUID id) {

        return ResponseEntity.ok(appointmentService.getPatientEncounterDetail(authId, id));
    }
}
