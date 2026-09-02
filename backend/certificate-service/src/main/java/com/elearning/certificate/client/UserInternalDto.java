package com.elearning.certificate.client;

/** Mirrors auth-service's UserInternalResponse. */
public record UserInternalDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {
    public String fullName() {
        return firstName + " " + lastName;
    }
}
