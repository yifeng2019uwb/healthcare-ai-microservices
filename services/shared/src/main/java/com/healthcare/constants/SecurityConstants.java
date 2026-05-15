package com.healthcare.constants;

public final class SecurityConstants {

    private SecurityConstants() {}

    // Internal HTTP headers — injected by gateway, read by all downstream services
    public static final String HEADER_USER_ID   = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    public static final String HEADER_USERNAME  = "X-Username";
    public static final String HEADER_FHIR_ID   = "X-Fhir-Id";

    // JWT claim keys
    public static final String JWT_CLAIM_ROLE     = "role";
    public static final String JWT_CLAIM_USERNAME = "username";
    public static final String JWT_CLAIM_EMAIL    = "email";
    public static final String JWT_CLAIM_FHIR_ID  = "fhirId";

    // Role values — mirrors UserRole enum
    public static final String ROLE_PATIENT  = "PATIENT";
    public static final String ROLE_PROVIDER = "PROVIDER";
    public static final String ROLE_ADMIN    = "ADMIN";
}
