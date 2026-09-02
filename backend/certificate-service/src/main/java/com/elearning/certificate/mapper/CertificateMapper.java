package com.elearning.certificate.mapper;

import com.elearning.certificate.config.FrontendProperties;
import com.elearning.certificate.dto.response.CertificateResponse;
import com.elearning.certificate.dto.response.CertificateVerificationResponse;
import com.elearning.certificate.entity.Certificate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CertificateMapper {

    private final FrontendProperties frontendProperties;

    public CertificateResponse toResponse(Certificate certificate) {
        return CertificateResponse.builder()
                .id(certificate.getId())
                .courseId(certificate.getCourseId())
                .courseTitle(certificate.getCourseTitle())
                .studentName(certificate.getStudentName())
                .verificationCode(certificate.getVerificationCode())
                .issuedAt(certificate.getIssuedAt())
                .verificationUrl(frontendProperties.getBaseUrl() + "/certificates/verify/" + certificate.getVerificationCode())
                .build();
    }

    public CertificateVerificationResponse toVerificationResponse(Certificate certificate) {
        return CertificateVerificationResponse.builder()
                .valid(true)
                .studentName(certificate.getStudentName())
                .courseTitle(certificate.getCourseTitle())
                .issuedAt(certificate.getIssuedAt())
                .verificationCode(certificate.getVerificationCode())
                .build();
    }
}
