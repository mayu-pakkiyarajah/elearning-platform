package com.elearning.certificate.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/** What the owning student sees — metadata, not the PDF bytes themselves. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String studentName;
    private String verificationCode;
    private LocalDateTime issuedAt;
    /** Public link anyone can open to verify + view this certificate, no login required. */
    private String verificationUrl;
}
