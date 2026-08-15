package com.elearning.course.controller;

import com.elearning.course.dto.request.CourseRequest;
import com.elearning.course.dto.response.CourseResponse;
import com.elearning.course.dto.response.CourseSummaryResponse;
import com.elearning.course.dto.response.PageResponse;
import com.elearning.course.security.AuthenticatedUser;
import com.elearning.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course catalog browsing and instructor management")
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    @Operation(summary = "Browse published courses with optional filters")
    public ResponseEntity<PageResponse<CourseSummaryResponse>> browse(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search,
            @Parameter(hidden = true) Pageable pageable
    ) {
        return ResponseEntity.ok(courseService.browse(categoryId, level, language, search, pageable));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get full course detail (sections + lessons) by slug")
    public ResponseEntity<CourseResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getBySlug(slug));
    }

    @GetMapping("/mine")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List the current instructor's own courses, including drafts")
    public ResponseEntity<List<CourseResponse>> listMine(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(courseService.listMine(currentUser));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create a new course (starts as DRAFT)")
    public ResponseEntity<CourseResponse> create(
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update a course (owner instructor or admin)")
    public ResponseEntity<CourseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CourseRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(courseService.update(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a course (owner instructor or admin)")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        courseService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Publish a course, making it visible in the public catalog")
    public ResponseEntity<CourseResponse> publish(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(courseService.setPublished(id, true, currentUser));
    }

    @PatchMapping("/{id}/unpublish")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unpublish a course, reverting it to DRAFT")
    public ResponseEntity<CourseResponse> unpublish(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(courseService.setPublished(id, false, currentUser));
    }
}
