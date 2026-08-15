package com.elearning.course.controller;

import com.elearning.course.dto.request.LessonRequest;
import com.elearning.course.dto.response.LessonResponse;
import com.elearning.course.security.AuthenticatedUser;
import com.elearning.course.service.LessonService;
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
@Tag(name = "Lessons", description = "Section lessons — owning instructor or admin only")
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/api/v1/sections/{sectionId}/lessons")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Add a lesson to a section")
    public ResponseEntity<LessonResponse> create(
            @PathVariable Long sectionId,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(sectionId, request, currentUser));
    }

    @PutMapping("/api/v1/lessons/{id}")
    @Operation(summary = "Update a lesson")
    public ResponseEntity<LessonResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody LessonRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(lessonService.update(id, request, currentUser));
    }

    @DeleteMapping("/api/v1/lessons/{id}")
    @Operation(summary = "Delete a lesson")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        lessonService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
