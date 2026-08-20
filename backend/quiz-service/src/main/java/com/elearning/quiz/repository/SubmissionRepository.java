package com.elearning.quiz.repository;

import com.elearning.quiz.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByQuizIdAndStudentIdOrderBySubmittedAtDesc(Long quizId, Long studentId);
    List<Submission> findByQuizIdOrderBySubmittedAtDesc(Long quizId);
    long countByQuizIdAndStudentId(Long quizId, Long studentId);
}
