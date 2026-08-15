CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    slug        VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE courses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    -- no FK to a local table: the owning user lives in auth-service's own DB.
    -- each microservice owns its own data; cross-service identity is just an id.
    instructor_id   BIGINT NOT NULL,
    category_id     BIGINT,
    title           VARCHAR(200) NOT NULL,
    slug            VARCHAR(220) NOT NULL UNIQUE,
    subtitle        VARCHAR(300),
    description     TEXT,
    level           VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',   -- BEGINNER | INTERMEDIATE | ADVANCED
    language        VARCHAR(50)  NOT NULL DEFAULT 'English',
    price           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    thumbnail_url   VARCHAR(500),
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',      -- DRAFT | PUBLISHED | ARCHIVED
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_category FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL
);

CREATE TABLE sections (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    title       VARCHAR(200) NOT NULL,
    position    INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_sections_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

CREATE TABLE lessons (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id      BIGINT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    position        INT NOT NULL DEFAULT 0,
    content_type    VARCHAR(20) NOT NULL DEFAULT 'VIDEO',  -- VIDEO | DOCUMENT | TEXT
    video_url       VARCHAR(500),
    duration_seconds INT,
    text_content    LONGTEXT,
    is_preview      BOOLEAN NOT NULL DEFAULT FALSE,          -- free preview, watchable without enrolling
    CONSTRAINT fk_lessons_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE CASCADE
);

CREATE TABLE course_files (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT NOT NULL,
    lesson_id   BIGINT,                                        -- nullable: file can belong to the course generally
    file_name   VARCHAR(255) NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    file_type   VARCHAR(50),
    size_bytes  BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_files_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT fk_course_files_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE
);

CREATE INDEX idx_courses_instructor_id ON courses (instructor_id);
CREATE INDEX idx_courses_category_id ON courses (category_id);
CREATE INDEX idx_courses_status ON courses (status);
CREATE INDEX idx_sections_course_id ON sections (course_id);
CREATE INDEX idx_lessons_section_id ON lessons (section_id);
CREATE INDEX idx_course_files_course_id ON course_files (course_id);

INSERT INTO categories (name, slug, description) VALUES
    ('Web Development', 'web-development', 'HTML, CSS, JavaScript, frontend and backend frameworks'),
    ('Data Science', 'data-science', 'Statistics, machine learning, data analysis'),
    ('Mobile Development', 'mobile-development', 'iOS, Android, cross-platform app development'),
    ('Business', 'business', 'Entrepreneurship, management, marketing'),
    ('Design', 'design', 'UI/UX, graphic design, product design');
