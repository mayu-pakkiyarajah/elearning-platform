package com.elearning.enrollment.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String status;
    private int totalLessons;
    private int completedLessons;
    private int progressPercent;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
}
