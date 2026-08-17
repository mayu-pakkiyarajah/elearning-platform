package com.elearning.enrollment.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/** For the instructor's "view enrolled students" roster — no student PII beyond
 *  the id (auth-service owns names/emails; a real roster page would batch-fetch
 *  those from auth-service by id, which isn't built yet). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrolledStudentResponse {
    private Long studentId;
    private String status;
    private int progressPercent;
    private LocalDateTime enrolledAt;
}
