package com.healthcare.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Appointment booking endpoints — Phase 2.
 *
 * Path: /api/appointments
 *
 * Planned endpoints:
 *   POST /api/appointments               — provider creates appointment slot for patient
 *   GET  /api/appointments/slots         — browse available slots
 *   PUT  /api/appointments/{id}/book     — book a slot (PATIENT or PROVIDER)
 *   PUT  /api/appointments/{id}/cancel   — cancel a slot (PATIENT or PROVIDER)
 *
 * See docs/appointment-service-design.md Phase 2 section.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    // Phase 2 — not yet implemented
}
