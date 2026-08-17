package com.elearning.enrollment.repository;

import com.elearning.enrollment.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);
    void deleteByEnrollmentIdAndLessonId(Long enrollmentId, Long lessonId);
}
