package com.elearning.quiz.service;

import com.elearning.quiz.dto.request.QuestionRequest;
import com.elearning.quiz.dto.request.QuizRequest;
import com.elearning.quiz.dto.request.SubmitQuizRequest;
import com.elearning.quiz.dto.response.*;
import com.elearning.quiz.security.AuthenticatedUser;

import java.util.List;

public interface QuizService {

    QuizResponse createQuiz(Long courseId, QuizRequest request, AuthenticatedUser currentUser);

    QuizResponse updateQuiz(Long quizId, QuizRequest request, AuthenticatedUser currentUser);

    void deleteQuiz(Long quizId, AuthenticatedUser currentUser);

    /** Requires the caller to either own the course (instructor/admin) or be enrolled in it. */
    List<QuizResponse> listQuizzesForCourse(Long courseId, AuthenticatedUser currentUser, String bearerToken);

    QuizDetailResponse getQuizDetail(Long quizId, AuthenticatedUser currentUser);

    QuestionResponse addQuestion(Long quizId, QuestionRequest request, AuthenticatedUser currentUser);

    QuestionResponse updateQuestion(Long questionId, QuestionRequest request, AuthenticatedUser currentUser);

    void deleteQuestion(Long questionId, AuthenticatedUser currentUser);

    /** Requires the caller to be enrolled in the quiz's course. Never includes correct answers. */
    QuizTakeResponse getQuizToTake(Long quizId, AuthenticatedUser currentUser, String bearerToken);

    SubmissionDetailResponse submitQuiz(Long quizId, SubmitQuizRequest request, AuthenticatedUser currentUser, String bearerToken);

    List<SubmissionResponse> listMySubmissions(Long quizId, AuthenticatedUser currentUser);

    /** Owning instructor/admin only. */
    List<SubmissionResponse> listSubmissionsForQuiz(Long quizId, AuthenticatedUser currentUser);
}
