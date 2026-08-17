package com.elearning.enrollment.service;

import com.elearning.enrollment.dto.response.EnrolledStudentResponse;
import com.elearning.enrollment.dto.response.EnrollmentDetailResponse;
import com.elearning.enrollment.dto.response.EnrollmentResponse;
import com.elearning.enrollment.security.AuthenticatedUser;

import java.util.List;

public interface EnrollmentService {

    /** Idempotent: enrolling twice in the same course returns the existing enrollment. */
    EnrollmentResponse enroll(Long courseId, AuthenticatedUser currentUser);

    List<EnrollmentResponse> listMine(AuthenticatedUser currentUser);

    EnrollmentDetailResponse getDetail(Long courseId, AuthenticatedUser currentUser);

    EnrollmentDetailResponse markLessonComplete(Long courseId, Long lessonId, AuthenticatedUser currentUser);

    EnrollmentDetailResponse markLessonIncomplete(Long courseId, Long lessonId, AuthenticatedUser currentUser);

    /** Instructor-only: roster for a course they own — ownership verified via course-service. */
    List<EnrolledStudentResponse> listStudentsForCourse(Long courseId, AuthenticatedUser currentUser);
}
