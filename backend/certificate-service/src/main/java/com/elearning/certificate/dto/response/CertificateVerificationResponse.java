package com.elearning.certificate.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/** Returned from the public verify endpoint — deliberately minimal, no internal ids. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateVerificationResponse {
    private boolean valid;
    private String studentName;
    private String courseTitle;
    private LocalDateTime issuedAt;
    private String verificationCode;
}
