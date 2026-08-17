CREATE TABLE enrollments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- both reference rows owned by OTHER services (auth-service, course-service) —
    -- no local FK possible, each microservice owns only its own tables.
    student_id      BIGINT NOT NULL,
    course_id       BIGINT NOT NULL,
    -- snapshots taken at enrollment time so this service can render a course list
    -- without calling course-service on every read. Title can go stale if the
    -- instructor renames the course later — acceptable trade-off for this scale.
    course_title    VARCHAR(200) NOT NULL,
    total_lessons   INT NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE | COMPLETED
    enrolled_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP NULL,
    UNIQUE KEY uq_enrollment_student_course (student_id, course_id)
);

CREATE TABLE lesson_progress (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id   BIGINT NOT NULL,
    lesson_id       BIGINT NOT NULL,
    completed_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lesson_progress_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments (id) ON DELETE CASCADE,
    UNIQUE KEY uq_lesson_progress_enrollment_lesson (enrollment_id, lesson_id)
);

CREATE INDEX idx_enrollments_student_id ON enrollments (student_id);
CREATE INDEX idx_enrollments_course_id ON enrollments (course_id);
CREATE INDEX idx_lesson_progress_enrollment_id ON lesson_progress (enrollment_id);
