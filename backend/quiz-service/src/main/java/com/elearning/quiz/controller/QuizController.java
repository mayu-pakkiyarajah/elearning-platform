package com.elearning.quiz.controller;

import com.elearning.quiz.dto.request.QuestionRequest;
import com.elearning.quiz.dto.request.QuizRequest;
import com.elearning.quiz.dto.response.QuestionResponse;
import com.elearning.quiz.dto.response.QuizDetailResponse;
import com.elearning.quiz.dto.response.QuizResponse;
import com.elearning.quiz.security.AuthenticatedUser;
import com.elearning.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Quizzes", description = "Quiz and question management (instructor) — see QuizTakingController for the student flow")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/api/v1/courses/{courseId}/quizzes")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create a quiz for a course you own")
    public ResponseEntity<QuizResponse> createQuiz(
            @PathVariable Long courseId,
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.createQuiz(courseId, request, currentUser));
    }

    @GetMapping("/api/v1/courses/{courseId}/quizzes")
    @Operation(summary = "List a course's quizzes — owning instructor/admin, or enrolled students")
    public ResponseEntity<List<QuizResponse>> listQuizzesForCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(quizService.listQuizzesForCourse(courseId, currentUser, bearerToken(request)));
    }

    @GetMapping("/api/v1/quizzes/{id}")
    @Operation(summary = "Full quiz detail with correct answers — owning instructor/admin only")
    public ResponseEntity<QuizDetailResponse> getQuizDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quizService.getQuizDetail(id, currentUser));
    }

    @PutMapping("/api/v1/quizzes/{id}")
    @Operation(summary = "Update a quiz's title/description/passing score")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quizService.updateQuiz(id, request, currentUser));
    }

    @DeleteMapping("/api/v1/quizzes/{id}")
    @Operation(summary = "Delete a quiz")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        quizService.deleteQuiz(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/v1/quizzes/{quizId}/questions")
    @Operation(summary = "Add a question (with its choices) to a quiz")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quizService.addQuestion(quizId, request, currentUser));
    }

    @PutMapping("/api/v1/questions/{id}")
    @Operation(summary = "Replace a question's text/points/choices")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quizService.updateQuestion(id, request, currentUser));
    }

    @DeleteMapping("/api/v1/questions/{id}")
    @Operation(summary = "Delete a question")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser currentUser) {
        quizService.deleteQuestion(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/quizzes/{id}/submissions")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "List all students' submissions for a quiz you own")
    public ResponseEntity<List<com.elearning.quiz.dto.response.SubmissionResponse>> listSubmissionsForQuiz(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quizService.listSubmissionsForQuiz(id, currentUser));
    }

    static String bearerToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }
}
