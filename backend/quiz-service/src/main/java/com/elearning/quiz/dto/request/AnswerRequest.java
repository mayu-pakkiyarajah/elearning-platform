package com.elearning.quiz.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerRequest {

    @NotNull(message = "questionId is required")
    private Long questionId;

    /** Nullable — a student may submit without answering every question. */
    private Long choiceId;
}
