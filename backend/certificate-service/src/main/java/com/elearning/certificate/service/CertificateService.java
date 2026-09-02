package com.elearning.certificate.service;

import com.elearning.certificate.dto.response.CertificateResponse;
import com.elearning.certificate.dto.response.CertificateVerificationResponse;
import com.elearning.certificate.entity.Certificate;
import com.elearning.certificate.security.AuthenticatedUser;

import java.util.List;

public interface CertificateService {

    /** Idempotent: calling this again for a course you already have a certificate for just returns it. */
    CertificateResponse generate(Long courseId, AuthenticatedUser currentUser, String bearerToken);

    List<CertificateResponse> listMine(AuthenticatedUser currentUser);

    /** Owner only. Returns the entity (not a DTO) since the controller needs the raw PDF bytes. */
    Certificate getOwnedCertificateOrThrow(Long certificateId, AuthenticatedUser currentUser);

    CertificateVerificationResponse verify(String verificationCode);

    /** Returns the entity so the controller can stream the PDF bytes — this is a public endpoint, no ownership check. */
    Certificate getByVerificationCodeOrThrow(String verificationCode);
}
