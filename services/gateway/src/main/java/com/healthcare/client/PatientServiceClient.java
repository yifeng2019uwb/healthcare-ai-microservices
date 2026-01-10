package com.healthcare.client;

import com.healthcare.api.model.CreatePatientInput;
import com.healthcare.api.model.CreatePatientOutput;
import com.healthcare.exception.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Client for calling Patient Service APIs
 */
@Component
public class PatientServiceClient {

    private final WebClient webClient;

    public PatientServiceClient(
            @Value("${services.patient.url:http://localhost:8081}") String patientServiceUrl,
            WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(patientServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * Create a patient account in the Patient Service
     *
     * @param request the patient account creation request
     * @return the patient service response
     */
    public Mono<CreatePatientOutput> createPatientAccount(CreatePatientInput request) {
        return webClient.post()
                .uri("/api/patients")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                    response -> response.bodyToMono(String.class)
                            .map(body -> new InternalException("Patient Service client error: " + body)))
                .onStatus(status -> status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                            .map(body -> new InternalException("Patient Service server error: " + body)))
                .bodyToMono(CreatePatientOutput.class);
    }
}
