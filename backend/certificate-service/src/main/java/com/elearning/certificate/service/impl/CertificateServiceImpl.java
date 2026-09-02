package com.elearning.certificate.service.impl;

import com.elearning.certificate.client.AuthServiceClient;
import com.elearning.certificate.client.EnrollmentDetailDto;
import com.elearning.certificate.client.EnrollmentServiceClient;
import com.elearning.certificate.client.UserInternalDto;
import com.elearning.certificate.config.FrontendProperties;
import com.elearning.certificate.dto.response.CertificateResponse;
import com.elearning.certificate.dto.response.CertificateVerificationResponse;
import com.elearning.certificate.entity.Certificate;
import com.elearning.certificate.exception.CourseNotCompletedException;
import com.elearning.certificate.exception.ForbiddenOperationException;
import com.elearning.certificate.exception.ResourceNotFoundException;
import com.elearning.certificate.mapper.CertificateMapper;
import com.elearning.certificate.pdf.CertificatePdfGenerator;
import com.elearning.certificate.repository.CertificateRepository;
import com.elearning.certificate.security.AuthenticatedUser;
import com.elearning.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateServiceImpl implements CertificateService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0/I/1 — avoids ambiguous characters on a printed certificate
    private static final int CODE_LENGTH = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CertificateRepository certificateRepository;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final AuthServiceClient authServiceClient;
    private final CertificatePdfGenerator pdfGenerator;
    private final CertificateMapper certificateMapper;
    private final FrontendProperties frontendProperties;

    @Override
    @Transactional
    public CertificateResponse generate(Long courseId, AuthenticatedUser currentUser, String bearerToken) {
        return certificateRepository.findByStudentIdAndCourseId(currentUser.userId(), courseId)
                .map(certificateMapper::toResponse)
                .orElseGet(() -> issueNewCertificate(courseId, currentUser, bearerToken));
    }

    private CertificateResponse issueNewCertificate(Long courseId, AuthenticatedUser currentUser, String bearerToken) {
        EnrollmentDetailDto enrollmentDetail = enrollmentServiceClient.getEnrollmentDetail(courseId, bearerToken);

        if (!enrollmentDetail.enrollment().isCompleted()) {
            throw new CourseNotCompletedException("Finish every lesson in this course before generating a certificate");
        }

        UserInternalDto student = authServiceClient.getUserById(currentUser.userId(), bearerToken);
        String verificationCode = generateUniqueVerificationCode();
        LocalDate issuedDate = LocalDate.now();
        String verificationUrl = frontendProperties.getBaseUrl() + "/certificates/verify/" + verificationCode;

        byte[] pdfBytes = pdfGenerator.generate(
                student.fullName(), enrollmentDetail.enrollment().courseTitle(), issuedDate, verificationCode, verificationUrl);

        Certificate certificate = Certificate.builder()
                .studentId(currentUser.userId())
                .studentName(student.fullName())
                .courseId(courseId)
                .courseTitle(enrollmentDetail.enrollment().courseTitle())
                .verificationCode(verificationCode)
                .pdfData(pdfBytes)
                .build();

        Certificate saved = certificateRepository.save(certificate);
        log.info("Issued certificate {} to student {} for course {}", verificationCode, currentUser.userId(), courseId);

        return certificateMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateResponse> listMine(AuthenticatedUser currentUser) {
        return certificateRepository.findByStudentId(currentUser.userId()).stream()
                .map(certificateMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Certificate getOwnedCertificateOrThrow(Long certificateId, AuthenticatedUser currentUser) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate not found: " + certificateId));

        boolean isOwner = certificate.getStudentId().equals(currentUser.userId());
        boolean isAdmin = currentUser.hasRole(ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You do not have permission to access this certificate");
        }
        return certificate;
    }

    @Override
    @Transactional(readOnly = true)
    public CertificateVerificationResponse verify(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .map(certificateMapper::toVerificationResponse)
                .orElseGet(() -> CertificateVerificationResponse.builder()
                        .valid(false)
                        .verificationCode(verificationCode)
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Certificate getByVerificationCodeOrThrow(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new ResourceNotFoundException("No certificate found for that verification code"));
    }

    private String generateUniqueVerificationCode() {
        String candidate;
        do {
            candidate = randomCode();
        } while (certificateRepository.existsByVerificationCode(candidate));
        return candidate;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(SECURE_RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
