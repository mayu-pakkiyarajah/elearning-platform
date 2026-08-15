package com.elearning.course.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotNull(message = "Position is required")
    private Integer position;

    @NotNull(message = "Content type is required")
    @Pattern(regexp = "VIDEO|DOCUMENT|TEXT", message = "Content type must be VIDEO, DOCUMENT, or TEXT")
    private String contentType;

    @Size(max = 500)
    private String videoUrl;

    @PositiveOrZero
    private Integer durationSeconds;

    private String textContent;

    private boolean preview;
}
