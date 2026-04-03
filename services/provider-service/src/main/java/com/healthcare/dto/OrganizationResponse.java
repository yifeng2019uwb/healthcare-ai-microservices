package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Organization;

import java.util.UUID;

public record OrganizationResponse(
        @JsonProperty("id")      UUID id,
        @JsonProperty("name")    String name,
        @JsonProperty("address") String address,
        @JsonProperty("city")    String city,
        @JsonProperty("state")   String state,
        @JsonProperty("phone")   String phone) {

    public static OrganizationResponse from(Organization org) {
        if (org == null) return null;
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getAddress(),
                org.getCity(),
                org.getState(),
                org.getPhone());
    }
}
