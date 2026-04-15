package com.healthcare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.dto.AllergyResponse;
import com.healthcare.dto.ConditionResponse;
import com.healthcare.dto.PatientProfileResponse;
import com.healthcare.dto.PatientSummaryResponse;
import com.healthcare.dto.ProviderProfileResponse;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.dto.RegisterPatientResponse;
import com.healthcare.exception.ProviderExceptionHandler;
import com.healthcare.exception.ProviderServiceException;
import com.healthcare.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
@Import(ProviderExceptionHandler.class)
class ProviderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  ProviderService providerService;

    private static final UUID AUTH_ID    = UUID.randomUUID();
    private static final UUID PATIENT_ID = UUID.randomUUID();

    private ProviderProfileResponse providerProfile() {
        return new ProviderProfileResponse(
                UUID.randomUUID(), "PRV-000001", "Dr. Smith", "M",
                "Cardiology", null, "+15551234567", "LIC-001", null, "Bio text", true);
    }

    private PatientProfileResponse patientProfile() {
        return new PatientProfileResponse(
                PATIENT_ID, "MRN-000001", "John", null, "Doe",
                LocalDate.of(1990, 1, 15), "M", null, null,
                null, null, null, null, null, null, null, null, null, null);
    }

    // -------------------------------------------------------------------------
    // GET /api/provider/me
    // -------------------------------------------------------------------------

    @Test
    void getProfile_returns200_withProfile() throws Exception {
        when(providerService.getProfile(AUTH_ID)).thenReturn(providerProfile());

        mockMvc.perform(get("/api/provider/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider_code").value("PRV-000001"))
                .andExpect(jsonPath("$.name").value("Dr. Smith"));
    }

    @Test
    void getProfile_returns404_whenNotFound() throws Exception {
        when(providerService.getProfile(AUTH_ID))
                .thenThrow(new ProviderServiceException(HttpStatus.NOT_FOUND,
                        ProviderServiceException.PROVIDER_NOT_FOUND, "Not found"));

        mockMvc.perform(get("/api/provider/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/provider/me"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // POST /api/provider/patients/onboard
    // -------------------------------------------------------------------------

    @Test
    void onboardPatient_returns201_withResponse() throws Exception {
        RegisterPatientRequest req = new RegisterPatientRequest(
                "John", null, "Doe", LocalDate.of(1990, 1, 15),
                "M", null, null, null, null, null, null, null, null, null, null);
        RegisterPatientResponse resp = new RegisterPatientResponse(
                PATIENT_ID, "MRN-000001", "John", "Doe",
                LocalDate.of(1990, 1, 15), "M", null);
        when(providerService.onboardPatient(eq(AUTH_ID), any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/provider/patients/onboard")
                        .header("X-User-Id", AUTH_ID.toString())
                        .header("X-Username", "dr_smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mrn").value("MRN-000001"))
                .andExpect(jsonPath("$.first_name").value("John"));
    }

    @Test
    void onboardPatient_returns400_whenMissingFirstName() throws Exception {
        RegisterPatientRequest req = new RegisterPatientRequest(
                null, null, "Doe", null, null, null, null,
                null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/provider/patients/onboard")
                        .header("X-User-Id", AUTH_ID.toString())
                        .header("X-Username", "dr_smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onboardPatient_returns400_whenMissingHeader() throws Exception {
        RegisterPatientRequest req = new RegisterPatientRequest(
                "John", null, "Doe", null, null, null, null,
                null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/provider/patients/onboard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/provider/patients
    // -------------------------------------------------------------------------

    @Test
    void getPatients_returns200_withList() throws Exception {
        PatientSummaryResponse summary = new PatientSummaryResponse(
                PATIENT_ID, "MRN-000001", "John", "Doe",
                LocalDate.of(1990, 1, 15), "M", null, null);
        when(providerService.getPatients(eq(AUTH_ID), any())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/provider/patients")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mrn").value("MRN-000001"));
    }

    @Test
    void getPatients_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/provider/patients"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/provider/patients/{id}
    // -------------------------------------------------------------------------

    @Test
    void getPatient_returns200_withProfile() throws Exception {
        when(providerService.getPatient(AUTH_ID, PATIENT_ID)).thenReturn(patientProfile());

        mockMvc.perform(get("/api/provider/patients/{id}", PATIENT_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mrn").value("MRN-000001"));
    }

    @Test
    void getPatient_returns403_whenNoAccess() throws Exception {
        when(providerService.getPatient(AUTH_ID, PATIENT_ID))
                .thenThrow(new ProviderServiceException(HttpStatus.FORBIDDEN,
                        ProviderServiceException.ACCESS_DENIED, "Forbidden"));

        mockMvc.perform(get("/api/provider/patients/{id}", PATIENT_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPatient_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/provider/patients/{id}", PATIENT_ID))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/provider/patients/{id}/conditions
    // -------------------------------------------------------------------------

    @Test
    void getPatientConditions_returns200_withList() throws Exception {
        when(providerService.getPatientConditions(AUTH_ID, PATIENT_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/provider/patients/{id}/conditions", PATIENT_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPatientConditions_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/provider/patients/{id}/conditions", PATIENT_ID))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/provider/patients/{id}/allergies
    // -------------------------------------------------------------------------

    @Test
    void getPatientAllergies_returns200_withList() throws Exception {
        when(providerService.getPatientAllergies(AUTH_ID, PATIENT_ID)).thenReturn(List.of());

        mockMvc.perform(get("/api/provider/patients/{id}/allergies", PATIENT_ID)
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPatientAllergies_returns400_whenMissingHeader() throws Exception {
        mockMvc.perform(get("/api/provider/patients/{id}/allergies", PATIENT_ID))
                .andExpect(status().isBadRequest());
    }
}
