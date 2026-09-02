package com.elearning.certificate.client;

import java.util.List;

/** Mirrors enrollment-service's EnrollmentDetailResponse — only the fields this
 *  service actually needs are modeled (courseTitle, status). */
public record EnrollmentDetailDto(
        EnrollmentSummaryDto enrollment,
        List<Long> completedLessonIds
) {
}
