package com.healthcare.context;

import com.healthcare.enums.UserRole;

public interface JwtContextService {
    String getCurrentUserId();    // from X-User-Id header
    String getCurrentUsername();  // from X-Username header
    UserRole getCurrentRole();    // from X-User-Role header
    boolean hasRole(UserRole role);
    boolean isAuthenticated();
}
