package com.elearning.certificate.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateCertificateRequest {

    @NotNull(message = "courseId is required")
    private Long courseId;
}
