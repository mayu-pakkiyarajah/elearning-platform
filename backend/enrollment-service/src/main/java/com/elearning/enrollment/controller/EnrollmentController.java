package com.elearning.enrollment.controller;

import com.elearning.enrollment.dto.request.EnrollRequest;
import com.elearning.enrollment.dto.response.EnrolledStudentResponse;
import com.elearning.enrollment.dto.response.EnrollmentDetailResponse;
import com.elearning.enrollment.dto.response.EnrollmentResponse;
import com.elearning.enrollment.security.AuthenticatedUser;
import com.elearning.enrollment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Enrollments", description = "Student enrollment, lesson progress, course completion")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll in a course (idempotent — re-enrolling returns the existing enrollment)")
    public ResponseEntity<EnrollmentResponse> enroll(
            @Valid @RequestBody EnrollRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.enroll(request.getCourseId(), currentUser));
    }

    @GetMapping("/mine")
    @Operation(summary = "List the current student's enrollments")
    public ResponseEntity<List<EnrollmentResponse>> listMine(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(enrollmentService.listMine(currentUser));
    }

    @GetMapping("/mine/{courseId}")
    @Operation(summary = "Get enrollment + per-lesson completion for one course")
    public ResponseEntity<EnrollmentDetailResponse> getDetail(
            @PathVariable Long courseId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.getDetail(courseId, currentUser));
    }

    @PostMapping("/mine/{courseId}/lessons/{lessonId}/complete")
    @Operation(summary = "Mark a lesson complete (auto-completes the course once all lessons are done)")
    public ResponseEntity<EnrollmentDetailResponse> markLessonComplete(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.markLessonComplete(courseId, lessonId, currentUser));
    }

    @DeleteMapping("/mine/{courseId}/lessons/{lessonId}/complete")
    @Operation(summary = "Unmark a lesson as complete")
    public ResponseEntity<EnrollmentDetailResponse> markLessonIncomplete(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.markLessonIncomplete(courseId, lessonId, currentUser));
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Instructor: list students enrolled in a course they own")
    public ResponseEntity<List<EnrolledStudentResponse>> listStudentsForCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(enrollmentService.listStudentsForCourse(courseId, currentUser));
    }
}
