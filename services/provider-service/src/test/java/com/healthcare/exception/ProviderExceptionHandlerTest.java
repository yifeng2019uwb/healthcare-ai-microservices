package com.healthcare.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.controller.ProviderController;
import com.healthcare.dto.RegisterPatientRequest;
import com.healthcare.service.ProviderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
@Import(ProviderExceptionHandler.class)
class ProviderExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  ProviderService providerService;

    private static final UUID AUTH_ID = UUID.randomUUID();

    @Test
    void returns404_forProviderNotFoundException() throws Exception {
        when(providerService.getProfile(any()))
                .thenThrow(new ProviderServiceException(
                        HttpStatus.NOT_FOUND, ProviderServiceException.PROVIDER_NOT_FOUND, "Not found"));

        mockMvc.perform(get("/api/provider/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"));
    }

    @Test
    void returns403_forAccessDeniedException() throws Exception {
        when(providerService.getProfile(any()))
                .thenThrow(new ProviderServiceException(
                        HttpStatus.FORBIDDEN, ProviderServiceException.ACCESS_DENIED, "Forbidden"));

        mockMvc.perform(get("/api/provider/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void returns500_forUnexpectedException() throws Exception {
        when(providerService.getProfile(any()))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/api/provider/me")
                        .header("X-User-Id", AUTH_ID.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void returns400_forMissingHeader() throws Exception {
        mockMvc.perform(get("/api/provider/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returns400_forMethodArgumentNotValid_withFieldMessage() throws Exception {
        // first_name is @NotBlank — sending blank triggers MethodArgumentNotValidException
        RegisterPatientRequest req = new RegisterPatientRequest(
                "", null, "Doe", null, null, null, null,
                null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/provider/patients/onboard")
                        .header("X-User-Id", AUTH_ID.toString())
                        .header("X-Username", "dr_smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("firstName")));
    }

    @Test
    void returns400_forMalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/provider/patients/onboard")
                        .header("X-User-Id", AUTH_ID.toString())
                        .header("X-Username", "dr_smith")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }
}
