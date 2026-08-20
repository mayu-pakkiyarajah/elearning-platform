package com.elearning.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitQuizRequest {

    @NotNull(message = "Answers are required")
    @Valid
    private List<AnswerRequest> answers;
}
