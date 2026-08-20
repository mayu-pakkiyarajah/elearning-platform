CREATE TABLE quizzes (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- owned by course-service, no local FK possible across services
    course_id               BIGINT NOT NULL,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT,
    passing_score_percent   INT NOT NULL DEFAULT 70,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id     BIGINT NOT NULL,
    text        TEXT NOT NULL,
    position    INT NOT NULL DEFAULT 0,
    points      INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_questions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE
);

CREATE TABLE choices (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    text        VARCHAR(500) NOT NULL,
    position    INT NOT NULL DEFAULT 0,
    is_correct  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_choices_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE
);

CREATE TABLE submissions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id         BIGINT NOT NULL,
    -- owned by auth-service, no local FK possible across services
    student_id      BIGINT NOT NULL,
    attempt_number  INT NOT NULL DEFAULT 1,
    score_percent   INT NOT NULL DEFAULT 0,
    passed          BOOLEAN NOT NULL DEFAULT FALSE,
    submitted_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_submissions_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes (id) ON DELETE CASCADE
);

CREATE TABLE submission_answers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id   BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    choice_id       BIGINT NULL,   -- nullable: student may leave a question unanswered
    CONSTRAINT fk_submission_answers_submission FOREIGN KEY (submission_id) REFERENCES submissions (id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_answers_question FOREIGN KEY (question_id) REFERENCES questions (id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_answers_choice FOREIGN KEY (choice_id) REFERENCES choices (id) ON DELETE SET NULL
);

CREATE INDEX idx_quizzes_course_id ON quizzes (course_id);
CREATE INDEX idx_questions_quiz_id ON questions (quiz_id);
CREATE INDEX idx_choices_question_id ON choices (question_id);
CREATE INDEX idx_submissions_quiz_id ON submissions (quiz_id);
CREATE INDEX idx_submissions_student_id ON submissions (student_id);
CREATE INDEX idx_submission_answers_submission_id ON submission_answers (submission_id);
