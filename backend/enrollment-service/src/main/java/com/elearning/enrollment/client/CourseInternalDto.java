package com.elearning.enrollment.client;

/** Mirrors course-service's CourseInternalResponse. Kept as a plain record here
 *  since the two services don't share a library — each owns its own copy of the
 *  contract, matching whatever course-service actually returns. */
public record CourseInternalDto(
        Long id,
        String title,
        String slug,
        String status,
        Long instructorId,
        int totalLessons
) {
    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }
}
