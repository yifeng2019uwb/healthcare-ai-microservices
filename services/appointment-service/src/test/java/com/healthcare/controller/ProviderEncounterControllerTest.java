package com.healthcare.controller;

import com.healthcare.dto.EncounterDetailResponse;
import com.healthcare.dto.EncounterPageResponse;
import com.healthcare.dto.EncounterSummaryResponse;
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

@WebMvcTest(ProviderEncounterController.class)
@Import(AppointmentExceptionHandler.class)
class ProviderEncounterControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  AppointmentService appointmentService;

    private static final UUID AUTH_ID      = UUID.randomUUID();
    private static final UUID ENCOUNTER_ID = UUID.randomUUID();
    private static final UUID PATIENT_ID   = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // GET /api/encounters/provider
    // -------------------------------------------------------------------------

    @Test
    void getEncounters_returns200_withPagedResult() throws Exception {
        EncounterPageResponse page = new EncounterPageResponse(0L, 1, 10, List.of());
        when(appointmentService.getProviderEncounters(eq(AUTH_ID), isNull(), isNull(), isNull(), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/encounters/provider")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void getEncounters_returns200_withPatientIdFilter() throws Exception {
        EncounterPageResponse page = new EncounterPageResponse(0L, 1, 10, List.of());
        when(appointmentService.getProviderEncounters(any(), any(), any(), eq(PATIENT_ID), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/encounters/provider")
                        .header("X-User-Id", AUTH_ID.toString())
                        .param("patient_id", PATIENT_ID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getEncounters_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/encounters/provider"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEncounters_returns404_whenProviderNotFound() throws Exception {
        when(appointmentService.getProviderEncounters(any(), any(), any(), any(), any(int.class), any(int.class)))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.NOT_FOUND, AppointmentServiceException.PROVIDER_NOT_FOUND, "Not found"));

        mockMvc.perform(get("/api/encounters/provider")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/encounters/provider/{id}
    // -------------------------------------------------------------------------

    @Test
    void getEncounterDetail_returns200() throws Exception {
        EncounterDetailResponse detail = new EncounterDetailResponse(
                ENCOUNTER_ID, null, null, "ambulatory",
                "185349003", "Encounter for check up", null, null,
                null, null, null, null, null);
        when(appointmentService.getProviderEncounterDetail(AUTH_ID, ENCOUNTER_ID)).thenReturn(detail);

        mockMvc.perform(get("/api/encounters/provider/{id}", ENCOUNTER_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounter_class").value("ambulatory"));
    }

    @Test
    void getEncounterDetail_returns403_whenAccessDenied() throws Exception {
        when(appointmentService.getProviderEncounterDetail(any(), any()))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.FORBIDDEN, AppointmentServiceException.ACCESS_DENIED, "Forbidden"));

        mockMvc.perform(get("/api/encounters/provider/{id}", ENCOUNTER_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getEncounterDetail_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/encounters/provider/{id}", ENCOUNTER_ID))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/encounters/provider/patients/{patientId}
    // -------------------------------------------------------------------------

    @Test
    void getPatientEncounters_returns200_withList() throws Exception {
        when(appointmentService.getPatientEncountersByProvider(AUTH_ID, PATIENT_ID))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/encounters/provider/patients/{patientId}", PATIENT_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPatientEncounters_returns403_whenNoAccess() throws Exception {
        when(appointmentService.getPatientEncountersByProvider(any(), any()))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.FORBIDDEN, AppointmentServiceException.ACCESS_DENIED, "Forbidden"));

        mockMvc.perform(get("/api/encounters/provider/patients/{patientId}", PATIENT_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPatientEncounters_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/encounters/provider/patients/{patientId}", PATIENT_ID))
                .andExpect(status().isBadRequest());
    }
}
