package com.healthcare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.exception.AuthServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies a provider NPI against the CMS NPPES public registry.
 *
 * API docs: https://npiregistry.cms.hhs.gov/api-page
 * Demonstrates integration with a federal CMS identity system (CMS-9115-F context).
 *
 * Verification rules:
 *   1. NPI must exist in NPPES (result_count > 0)
 *   2. Provider's first + last name must match the NPPES record (case-insensitive)
 */
@Service
public class NpiVerificationService {

    private static final Logger log = LoggerFactory.getLogger(NpiVerificationService.class);

    private static final String NPPES_API_URL =
            "https://npiregistry.cms.hhs.gov/api/?number={npi}&version=2.1";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NpiVerificationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Verifies that the given NPI exists in NPPES and that the first/last name matches.
     *
     * @param npi       10-digit NPI string
     * @param firstName provider first name (from registration request)
     * @param lastName  provider last name (from registration request)
     * @throws AuthServiceException 400 if NPI not found or name doesn't match
     * @throws AuthServiceException 503 if NPPES API is unreachable
     */
    public void verify(String npi, String firstName, String lastName) {
        log.info("Verifying NPI {} against NPPES registry", npi);

        String responseBody;
        try {
            responseBody = restTemplate.getForObject(NPPES_API_URL, String.class, npi);
        } catch (RestClientException e) {
            log.error("NPPES API unreachable for NPI {}: {}", npi, e.getMessage());
            throw new AuthServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    AuthServiceException.NPI_VERIFICATION_FAILED,
                    "NPI registry is currently unavailable. Please try again later.");
        }

        if (responseBody == null) {
            throw new AuthServiceException(
                    HttpStatus.BAD_REQUEST,
                    AuthServiceException.NPI_VERIFICATION_FAILED,
                    "NPI not found in NPPES registry: " + npi);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            int resultCount = root.path("result_count").asInt(0);

            if (resultCount == 0) {
                log.warn("NPI {} not found in NPPES registry", npi);
                throw new AuthServiceException(
                        HttpStatus.BAD_REQUEST,
                        AuthServiceException.NPI_VERIFICATION_FAILED,
                        "NPI not found in NPPES registry: " + npi);
            }

            JsonNode basic = root.path("results").get(0).path("basic");
            String nppesFirst = basic.path("first_name").asText("").trim();
            String nppesLast  = basic.path("last_name").asText("").trim();

            if (!nppesFirst.equalsIgnoreCase(firstName.trim())
                    || !nppesLast.equalsIgnoreCase(lastName.trim())) {
                log.warn("NPI {} name mismatch: NPPES={} {} vs request={} {}",
                        npi, nppesFirst, nppesLast, firstName, lastName);
                throw new AuthServiceException(
                        HttpStatus.BAD_REQUEST,
                        AuthServiceException.NPI_VERIFICATION_FAILED,
                        "NPI name does not match registry record for NPI: " + npi);
            }

            log.info("NPI {} verified successfully for {} {}", npi, firstName, lastName);

        } catch (AuthServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse NPPES response for NPI {}: {}", npi, e.getMessage());
            throw new AuthServiceException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    AuthServiceException.NPI_VERIFICATION_FAILED,
                    "NPI registry returned an unexpected response. Please try again later.");
        }
    }
}
