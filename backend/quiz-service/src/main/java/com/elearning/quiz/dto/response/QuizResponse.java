package com.elearning.quiz.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/** Lightweight — for listing a course's quizzes. No question content. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer passingScorePercent;
    private int questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
