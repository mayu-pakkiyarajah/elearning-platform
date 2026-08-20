package com.elearning.quiz.client;

/** Mirrors course-service's CourseInternalResponse. */
public record CourseInternalDto(
        Long id,
        String title,
        String slug,
        String status,
        Long instructorId,
        int totalLessons
) {
}
