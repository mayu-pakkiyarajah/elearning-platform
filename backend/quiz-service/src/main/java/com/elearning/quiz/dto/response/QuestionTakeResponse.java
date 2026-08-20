package com.elearning.quiz.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionTakeResponse {
    private Long id;
    private String text;
    private Integer position;
    private Integer points;
    private List<ChoiceTakeResponse> choices;
}
