package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Provider;

import java.util.UUID;

public record ProviderProfileResponse(
        @JsonProperty("id")             UUID id,
        @JsonProperty("provider_code")  String providerCode,
        @JsonProperty("name")           String name,
        @JsonProperty("gender")         String gender,
        @JsonProperty("speciality")     String speciality,
        @JsonProperty("organization")   OrganizationResponse organization,
        @JsonProperty("phone")          String phone,
        @JsonProperty("license_number") String licenseNumber,
        @JsonProperty("bio")            String bio,
        @JsonProperty("is_active")      boolean isActive) {

    public static ProviderProfileResponse from(Provider p) {
        return new ProviderProfileResponse(
                p.getId(),
                p.getProviderCode(),
                p.getName(),
                p.getGender() != null ? p.getGender().name() : null,
                p.getSpeciality(),
                OrganizationResponse.from(p.getOrganization()),
                p.getPhone(),
                p.getLicenseNumber(),
                p.getBio(),
                p.isActive());
    }
}
