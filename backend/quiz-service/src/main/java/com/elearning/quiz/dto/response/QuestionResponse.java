package com.elearning.quiz.dto.response;

import lombok.*;

import java.util.List;

/** Instructor-facing — choices include correct flags. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResponse {
    private Long id;
    private String text;
    private Integer position;
    private Integer points;
    private List<ChoiceResponse> choices;
}
