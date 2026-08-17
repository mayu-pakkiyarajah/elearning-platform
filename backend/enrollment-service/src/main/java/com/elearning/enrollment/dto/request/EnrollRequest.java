package com.elearning.enrollment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;
}
