CREATE TABLE certificates (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- owned by auth-service and course-service respectively — no local FK possible
    student_id          BIGINT NOT NULL,
    student_name        VARCHAR(200) NOT NULL,   -- snapshot at issue time, same pattern as other services
    course_id           BIGINT NOT NULL,
    course_title        VARCHAR(200) NOT NULL,   -- snapshot at issue time
    verification_code   VARCHAR(20) NOT NULL UNIQUE,
    issued_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pdf_data            LONGBLOB NOT NULL,
    UNIQUE KEY uq_certificate_student_course (student_id, course_id)
);

CREATE INDEX idx_certificates_student_id ON certificates (student_id);
CREATE INDEX idx_certificates_verification_code ON certificates (verification_code);
