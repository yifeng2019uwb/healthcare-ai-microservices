package com.healthcare.controller;

import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.exception.AppointmentExceptionHandler;
import com.healthcare.exception.AppointmentServiceException;
import com.healthcare.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientAppointmentController.class)
@Import(AppointmentExceptionHandler.class)
class PatientAppointmentControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  AppointmentService appointmentService;

    private static final UUID AUTH_ID      = UUID.randomUUID();
    private static final UUID ENCOUNTER_ID = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // GET /api/appointments/me/encounters
    // -------------------------------------------------------------------------

    @Test
    void getEncounters_returns200_withPagedResult() throws Exception {
        EncounterPageResponse page = new EncounterPageResponse(0L, 1, 10, List.of());
        when(appointmentService.getPatientEncounters(eq(AUTH_ID), isNull(), isNull(), isNull(), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/appointments/me/encounters")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    void getEncounters_returns200_withFilters() throws Exception {
        EncounterPageResponse page = new EncounterPageResponse(0L, 1, 10, List.of());
        when(appointmentService.getPatientEncounters(any(), any(), any(), eq("ambulatory"), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/appointments/me/encounters")
                        .header("X-User-Id", AUTH_ID.toString())
                        .param("from", "2023-01-01")
                        .param("to", "2023-12-31")
                        .param("class", "ambulatory"))
                .andExpect(status().isOk());
    }

    @Test
    void getEncounters_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/appointments/me/encounters"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEncounters_returns404_whenPatientNotFound() throws Exception {
        when(appointmentService.getPatientEncounters(any(), any(), any(), any(), any(int.class), any(int.class)))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.NOT_FOUND, AppointmentServiceException.PATIENT_NOT_FOUND, "Not found"));

        mockMvc.perform(get("/api/appointments/me/encounters")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/appointments/me/encounters/{id}
    // -------------------------------------------------------------------------

    @Test
    void getEncounterDetail_returns200() throws Exception {
        EncounterDetailResponse detail = new EncounterDetailResponse(
                ENCOUNTER_ID, null, null, "ambulatory",
                "185349003", "Encounter for check up", null, "Annual checkup",
                null, null, null, null, null);
        when(appointmentService.getPatientEncounterDetail(AUTH_ID, ENCOUNTER_ID)).thenReturn(detail);

        mockMvc.perform(get("/api/appointments/me/encounters/{id}", ENCOUNTER_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounter_class").value("ambulatory"))
                .andExpect(jsonPath("$.description").value("Encounter for check up"));
    }

    @Test
    void getEncounterDetail_returns403_whenAccessDenied() throws Exception {
        when(appointmentService.getPatientEncounterDetail(any(), any()))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.FORBIDDEN, AppointmentServiceException.ACCESS_DENIED, "Forbidden"));

        mockMvc.perform(get("/api/appointments/me/encounters/{id}", ENCOUNTER_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEncounterDetail_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/appointments/me/encounters/{id}", ENCOUNTER_ID))
                .andExpect(status().isBadRequest());
    }
}
