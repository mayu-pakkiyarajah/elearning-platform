package com.elearning.quiz.dto.response;

import lombok.*;

import java.util.List;

/** Instructor-facing full view — includes questions with correct-answer flags.
 *  NEVER returned from a student-facing "take this quiz" endpoint. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDetailResponse {
    private QuizResponse quiz;
    private List<QuestionResponse> questions;
}
