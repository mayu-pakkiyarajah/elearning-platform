package com.elearning.course.controller;

import com.elearning.course.dto.request.SectionRequest;
import com.elearning.course.dto.response.SectionResponse;
import com.elearning.course.security.AuthenticatedUser;
import com.elearning.course.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sections", description = "Course sections — owning instructor or admin only")
public class SectionController {

    private final SectionService sectionService;

    @PostMapping("/api/v1/courses/{courseId}/sections")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Add a section to a course")
    public ResponseEntity<SectionResponse> create(
            @PathVariable Long courseId,
            @Valid @RequestBody SectionRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sectionService.create(courseId, request, currentUser));
    }

    @PutMapping("/api/v1/sections/{id}")
    @Operation(summary = "Update a section")
    public ResponseEntity<SectionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(sectionService.update(id, request, currentUser));
    }

    @DeleteMapping("/api/v1/sections/{id}")
    @Operation(summary = "Delete a section (and its lessons)")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        sectionService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
