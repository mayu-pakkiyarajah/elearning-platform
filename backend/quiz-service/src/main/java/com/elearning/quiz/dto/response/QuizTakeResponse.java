package com.elearning.quiz.dto.response;

import lombok.*;

import java.util.List;

/** What a student sees when starting a quiz attempt — no correct answers included. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizTakeResponse {
    private Long id;
    private String title;
    private String description;
    private Integer passingScorePercent;
    private List<QuestionTakeResponse> questions;
}
