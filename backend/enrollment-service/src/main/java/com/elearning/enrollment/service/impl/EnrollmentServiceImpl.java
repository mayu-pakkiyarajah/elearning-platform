package com.elearning.enrollment.service.impl;

import com.elearning.enrollment.client.CourseInternalDto;
import com.elearning.enrollment.client.CourseServiceClient;
import com.elearning.enrollment.dto.response.EnrolledStudentResponse;
import com.elearning.enrollment.dto.response.EnrollmentDetailResponse;
import com.elearning.enrollment.dto.response.EnrollmentResponse;
import com.elearning.enrollment.entity.Enrollment;
import com.elearning.enrollment.entity.EnrollmentStatus;
import com.elearning.enrollment.entity.LessonProgress;
import com.elearning.enrollment.exception.CourseNotEnrollableException;
import com.elearning.enrollment.exception.ForbiddenOperationException;
import com.elearning.enrollment.exception.ResourceNotFoundException;
import com.elearning.enrollment.mapper.EnrollmentMapper;
import com.elearning.enrollment.repository.EnrollmentRepository;
import com.elearning.enrollment.repository.LessonProgressRepository;
import com.elearning.enrollment.security.AuthenticatedUser;
import com.elearning.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseServiceClient courseServiceClient;
    private final EnrollmentMapper enrollmentMapper;

    @Override
    @Transactional
    public EnrollmentResponse enroll(Long courseId, AuthenticatedUser currentUser) {
        return enrollmentRepository.findByStudentIdAndCourseId(currentUser.userId(), courseId)
                .map(enrollmentMapper::toResponse)
                .orElseGet(() -> createEnrollment(courseId, currentUser));
    }

    private EnrollmentResponse createEnrollment(Long courseId, AuthenticatedUser currentUser) {
        CourseInternalDto course = courseServiceClient.getCourseById(courseId);

        if (!course.isPublished()) {
            throw new CourseNotEnrollableException("This course isn't published yet");
        }

        Enrollment enrollment = Enrollment.builder()
                .studentId(currentUser.userId())
                .courseId(courseId)
                .courseTitle(course.title())
                .totalLessons(course.totalLessons())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Student {} enrolled in course {} ({})", currentUser.userId(), courseId, course.title());
        return enrollmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listMine(AuthenticatedUser currentUser) {
        return enrollmentRepository.findByStudentId(currentUser.userId()).stream()
                .map(enrollmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentDetailResponse getDetail(Long courseId, AuthenticatedUser currentUser) {
        Enrollment enrollment = getOwnEnrollmentOrThrow(courseId, currentUser);
        return toDetailResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse markLessonComplete(Long courseId, Long lessonId, AuthenticatedUser currentUser) {
        Enrollment enrollment = getOwnEnrollmentOrThrow(courseId, currentUser);

        boolean alreadyComplete = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId).isPresent();

        if (!alreadyComplete) {
            LessonProgress progress = LessonProgress.builder()
                    .enrollment(enrollment)
                    .lessonId(lessonId)
                    .build();
            enrollment.getCompletedLessons().add(progress);
            lessonProgressRepository.save(progress);
        }

        updateCompletionStatus(enrollment);
        return toDetailResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentDetailResponse markLessonIncomplete(Long courseId, Long lessonId, AuthenticatedUser currentUser) {
        Enrollment enrollment = getOwnEnrollmentOrThrow(courseId, currentUser);

        lessonProgressRepository.deleteByEnrollmentIdAndLessonId(enrollment.getId(), lessonId);
        enrollment.getCompletedLessons().removeIf(p -> p.getLessonId().equals(lessonId));

        // un-completing a lesson reopens a "finished" course
        if (enrollment.getStatus() == EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setCompletedAt(null);
        }

        return toDetailResponse(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrolledStudentResponse> listStudentsForCourse(Long courseId, AuthenticatedUser currentUser) {
        CourseInternalDto course = courseServiceClient.getCourseById(courseId);

        boolean isOwner = course.instructorId().equals(currentUser.userId());
        boolean isAdmin = currentUser.hasRole(ROLE_ADMIN);
        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You do not have permission to view this course's roster");
        }

        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(e -> EnrolledStudentResponse.builder()
                        .studentId(e.getStudentId())
                        .status(e.getStatus().name())
                        .progressPercent(e.progressPercent())
                        .enrolledAt(e.getEnrolledAt())
                        .build())
                .toList();
    }

    private void updateCompletionStatus(Enrollment enrollment) {
        boolean allDone = enrollment.getTotalLessons() != null
                && enrollment.getTotalLessons() > 0
                && enrollment.completedLessonCount() >= enrollment.getTotalLessons();

        if (allDone && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            enrollment.setStatus(EnrollmentStatus.COMPLETED);
            enrollment.setCompletedAt(LocalDateTime.now());
            log.info("Enrollment {} marked COMPLETED (student {}, course {})",
                    enrollment.getId(), enrollment.getStudentId(), enrollment.getCourseId());
            // TODO: publish an "EnrollmentCompleted" event once notification-service and
            // certificate-service exist, so a certificate can be generated automatically.
        }
    }

    private Enrollment getOwnEnrollmentOrThrow(Long courseId, AuthenticatedUser currentUser) {
        return enrollmentRepository.findByStudentIdAndCourseId(currentUser.userId(), courseId)
                .orElseThrow(() -> new ResourceNotFoundException("You are not enrolled in this course"));
    }

    private EnrollmentDetailResponse toDetailResponse(Enrollment enrollment) {
        List<Long> completedLessonIds = enrollment.getCompletedLessons().stream()
                .map(LessonProgress::getLessonId)
                .toList();

        return EnrollmentDetailResponse.builder()
                .enrollment(enrollmentMapper.toResponse(enrollment))
                .completedLessonIds(completedLessonIds)
                .build();
    }
}
