package com.healthcare.exception;

import com.healthcare.controller.PatientEncounterController;
import com.healthcare.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientEncounterController.class)
@Import(AppointmentExceptionHandler.class)
class AppointmentExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  AppointmentService appointmentService;

    private static final UUID AUTH_ID = UUID.randomUUID();

    @Test
    void returns404_forPatientNotFoundException() throws Exception {
        when(appointmentService.getPatientEncounters(any(), any(), any(), any(), any(int.class), any(int.class)))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.NOT_FOUND, AppointmentServiceException.PATIENT_NOT_FOUND, "Not found"));

        mockMvc.perform(get("/api/encounters/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"));
    }

    @Test
    void returns403_forAccessDeniedException() throws Exception {
        when(appointmentService.getPatientEncounterDetail(any(), any()))
                .thenThrow(new AppointmentServiceException(
                        HttpStatus.FORBIDDEN, AppointmentServiceException.ACCESS_DENIED, "Forbidden"));

        mockMvc.perform(get("/api/encounters/me/{id}", UUID.randomUUID())
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void returns500_forUnexpectedException() throws Exception {
        when(appointmentService.getPatientEncounters(any(), any(), any(), any(), any(int.class), any(int.class)))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/api/encounters/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void returns400_forMissingHeader() throws Exception {
        mockMvc.perform(get("/api/encounters/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
