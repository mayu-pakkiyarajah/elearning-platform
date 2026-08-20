package com.elearning.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoiceRequest {

    @NotBlank(message = "Choice text is required")
    @Size(max = 500)
    private String text;

    @NotNull(message = "Position is required")
    private Integer position;

    private boolean correct;
}
