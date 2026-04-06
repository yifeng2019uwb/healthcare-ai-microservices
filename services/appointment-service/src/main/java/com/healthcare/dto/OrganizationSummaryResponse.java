package com.healthcare.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.healthcare.entity.Organization;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrganizationSummaryResponse(
        UUID id,
        String name,
        String address,
        String city,
        String state,
        String phone) {

    /** Minimal view for encounter list. */
    public static OrganizationSummaryResponse summary(Organization o) {
        if (o == null) return null;
        return new OrganizationSummaryResponse(o.getId(), o.getName(), null, o.getCity(), null, null);
    }

    /** Full view for encounter detail. */
    public static OrganizationSummaryResponse detail(Organization o) {
        if (o == null) return null;
        return new OrganizationSummaryResponse(
                o.getId(), o.getName(), o.getAddress(), o.getCity(), o.getState(), o.getPhone());
    }
}
