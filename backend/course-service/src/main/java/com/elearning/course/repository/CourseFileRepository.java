package com.elearning.course.repository;

import com.elearning.course.entity.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseFileRepository extends JpaRepository<CourseFile, Long> {
    List<CourseFile> findByCourseId(Long courseId);
    List<CourseFile> findByLessonId(Long lessonId);
}
