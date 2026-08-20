package com.elearning.quiz.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResultResponse {
    private Long questionId;
    private Long selectedChoiceId;
    private Long correctChoiceId;
    private boolean correct;
}
