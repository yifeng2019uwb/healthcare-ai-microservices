package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.healthcare.entity.Provider;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProviderSummaryResponse(
        UUID id,
        String name,
        String speciality,
        String phone) {

    /** Minimal view for encounter list. */
    public static ProviderSummaryResponse summary(Provider p) {
        if (p == null) return null;
        return new ProviderSummaryResponse(p.getId(), p.getName(), p.getSpeciality(), null);
    }

    /** Full view for encounter detail. */
    public static ProviderSummaryResponse detail(Provider p) {
        if (p == null) return null;
        return new ProviderSummaryResponse(p.getId(), p.getName(), p.getSpeciality(), p.getPhone());
    }
}
