package com.elearning.enrollment.dto.response;

import lombok.*;

import java.util.List;

/** Enrollment summary + which specific lesson ids are complete — used by the
 *  lesson viewer to know which checkmarks to show. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentDetailResponse {
    private EnrollmentResponse enrollment;
    private List<Long> completedLessonIds;
}
