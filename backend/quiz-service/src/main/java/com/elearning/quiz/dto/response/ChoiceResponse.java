package com.elearning.quiz.dto.response;

import lombok.*;

/** Instructor-facing — includes which choice is correct. Never sent to a student taking the quiz. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceResponse {
    private Long id;
    private String text;
    private Integer position;
    private boolean correct;
}
