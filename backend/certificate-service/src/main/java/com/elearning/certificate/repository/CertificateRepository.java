package com.elearning.certificate.repository;

import com.elearning.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Certificate> findByStudentId(Long studentId);
    Optional<Certificate> findByVerificationCode(String verificationCode);
    boolean existsByVerificationCode(String verificationCode);
}
