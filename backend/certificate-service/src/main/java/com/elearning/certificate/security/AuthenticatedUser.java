package com.elearning.certificate.security;

import java.util.List;

public record AuthenticatedUser(
        Long userId,
        String email,
        List<String> roles
) {
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
