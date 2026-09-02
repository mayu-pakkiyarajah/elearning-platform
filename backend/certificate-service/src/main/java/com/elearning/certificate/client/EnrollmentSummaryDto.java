package com.elearning.certificate.client;

/** Mirrors the "enrollment" object inside enrollment-service's EnrollmentDetailResponse. */
public record EnrollmentSummaryDto(
        Long id,
        Long courseId,
        String courseTitle,
        String courseSlug,
        String status,
        Integer totalLessons,
        Integer completedLessons,
        Integer progressPercent,
        String enrolledAt,
        String completedAt
) {
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}
