package com.elearning.quiz.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {
    private Long id;
    private Long quizId;
    private Long studentId;
    private Integer attemptNumber;
    private Integer scorePercent;
    private boolean passed;
    private LocalDateTime submittedAt;
}
