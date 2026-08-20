package com.elearning.quiz.controller;

import com.elearning.quiz.dto.request.SubmitQuizRequest;
import com.elearning.quiz.dto.response.QuizTakeResponse;
import com.elearning.quiz.dto.response.SubmissionDetailResponse;
import com.elearning.quiz.dto.response.SubmissionResponse;
import com.elearning.quiz.security.AuthenticatedUser;
import com.elearning.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Quiz Taking", description = "Student-facing: take a quiz, submit answers, view own attempts")
public class QuizTakingController {

    private final QuizService quizService;

    @GetMapping("/{id}/take")
    @Operation(summary = "Get a quiz to take — requires enrollment, never includes correct answers")
    public ResponseEntity<QuizTakeResponse> getQuizToTake(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(quizService.getQuizToTake(id, currentUser, QuizController.bearerToken(request)));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit answers and get scored immediately")
    public ResponseEntity<SubmissionDetailResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitQuizRequest submitRequest,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(quizService.submitQuiz(id, submitRequest, currentUser, QuizController.bearerToken(request)));
    }

    @GetMapping("/{id}/submissions/mine")
    @Operation(summary = "List the current student's own past attempts at this quiz")
    public ResponseEntity<List<SubmissionResponse>> listMySubmissions(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        return ResponseEntity.ok(quizService.listMySubmissions(id, currentUser));
    }
}
