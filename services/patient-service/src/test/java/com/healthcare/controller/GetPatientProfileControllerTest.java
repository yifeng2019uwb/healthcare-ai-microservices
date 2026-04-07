package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.EncounterResponse;
import com.healthcare.dto.PageResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.UpdatePatientRequest;
import com.healthcare.exception.PatientExceptionHandler;
import com.healthcare.exception.PatientServiceException;
import com.healthcare.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetPatientProfileController.class)
@Import(PatientExceptionHandler.class)
class GetPatientProfileControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  PatientService patientService;

    private static final UUID AUTH_ID = UUID.randomUUID();

    private PatientProfileResponse profile() {
        return new PatientProfileResponse(
                UUID.randomUUID(), "MRN-000001", "John", null, "Doe",
                null, null, "1990-01-15", null, null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    // -------------------------------------------------------------------------
    // GET /api/patients/me
    // -------------------------------------------------------------------------

    @Test
    void getProfile_returns200_withProfile() throws Exception {
        when(patientService.getProfile(AUTH_ID)).thenReturn(profile());

        mockMvc.perform(get("/api/patients/me")
                        .header("X-User-Id", AUTH_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mrn").value("MRN-000001"))
                .andExpect(jsonPath("$.first_name").value("John"));
    }

    @Test
    void getProfile_returns404_whenNotFound() throws Exception {
        when(patientService.getProfile(AUTH_ID))
                .thenThrow(new PatientServiceException(HttpStatus.NOT_FOUND,
                        PatientServiceException.PATIENT_NOT_FOUND, "Not found"));

        mockMvc.perform(get("/api/patients/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/patients/me"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/patients/me
    // -------------------------------------------------------------------------

    @Test
    void updateProfile_returns200_withUpdatedProfile() throws Exception {
        UpdatePatientRequest req = new UpdatePatientRequest(
                "+15551234567", null, "123 Main St", "Seattle", "WA", "98101", null);
        when(patientService.updateProfile(eq(AUTH_ID), any(), any())).thenReturn(profile());

        mockMvc.perform(put("/api/patients/me")
                        .header("X-User-Id", AUTH_ID.toString())
                        .header("X-Username", "john_doe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mrn").value("MRN-000001"));
    }

    @Test
    void updateProfile_returns400_whenMissingHeader() throws Exception {
        UpdatePatientRequest req = new UpdatePatientRequest(
                null, null, null, null, null, null, null);

        mockMvc.perform(put("/api/patients/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/patients/me/encounters
    // -------------------------------------------------------------------------

    @Test
    void getEncounters_returns200_withPagedResults() throws Exception {
        PageResponse<EncounterResponse> page = new PageResponse<>(List.of(), 0L, 0, 20);
        when(patientService.getEncounters(eq(AUTH_ID), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/patients/me/encounters")
                        .header("X-User-Id", AUTH_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void getEncounters_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/patients/me/encounters"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/patients/me/conditions
    // -------------------------------------------------------------------------

    @Test
    void getConditions_returns200_withList() throws Exception {
        when(patientService.getConditions(AUTH_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/patients/me/conditions")
                        .header("X-User-Id", AUTH_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getConditions_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/patients/me/conditions"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/patients/me/allergies
    // -------------------------------------------------------------------------

    @Test
    void getAllergies_returns200_withList() throws Exception {
        when(patientService.getAllergies(AUTH_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/patients/me/allergies")
                        .header("X-User-Id", AUTH_ID.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllergies_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/patients/me/allergies"))
                .andExpect(status().isBadRequest());
    }
}
