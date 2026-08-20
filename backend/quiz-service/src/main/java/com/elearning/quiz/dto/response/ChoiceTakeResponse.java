package com.elearning.quiz.dto.response;

import lombok.*;

/** Student-facing — deliberately has NO correct flag. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceTakeResponse {
    private Long id;
    private String text;
    private Integer position;
}
