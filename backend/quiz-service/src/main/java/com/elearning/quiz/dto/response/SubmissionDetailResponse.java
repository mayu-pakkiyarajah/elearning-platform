package com.elearning.quiz.dto.response;

import lombok.*;

import java.util.List;

/** Returned right after submitting — shows the student what they got right/wrong. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionDetailResponse {
    private SubmissionResponse submission;
    private List<AnswerResultResponse> answers;
}
