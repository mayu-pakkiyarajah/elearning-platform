package com.elearning.course.security;

import java.util.List;

/**
 * This service never talks to auth-service's user table — it trusts the claims
 * inside a valid JWT (issued by auth-service) as the source of truth for "who is
 * this request from". This is what @AuthenticationPrincipal resolves to here.
 */
public record AuthenticatedUser(
        Long userId,
        String email,
        List<String> roles
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
