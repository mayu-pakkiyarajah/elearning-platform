package com.elearning.certificate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owned by auth-service — no local FK possible across services. */
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    /** Snapshotted at issue time, same pattern as course_title on enrollments. */
    @Column(name = "student_name", nullable = false, length = 200)
    private String studentName;

    /** Owned by course-service — no local FK possible across services. */
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "course_title", nullable = false, length = 200)
    private String courseTitle;

    @Column(name = "verification_code", nullable = false, unique = true, length = 20)
    private String verificationCode;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    @Lob
    @Column(name = "pdf_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] pdfData;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
    }
}
