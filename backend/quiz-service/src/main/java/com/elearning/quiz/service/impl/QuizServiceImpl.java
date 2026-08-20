package com.elearning.quiz.service.impl;

import com.elearning.quiz.client.CourseInternalDto;
import com.elearning.quiz.client.CourseServiceClient;
import com.elearning.quiz.client.EnrollmentServiceClient;
import com.elearning.quiz.dto.request.AnswerRequest;
import com.elearning.quiz.dto.request.ChoiceRequest;
import com.elearning.quiz.dto.request.QuestionRequest;
import com.elearning.quiz.dto.request.QuizRequest;
import com.elearning.quiz.dto.request.SubmitQuizRequest;
import com.elearning.quiz.dto.response.*;
import com.elearning.quiz.entity.*;
import com.elearning.quiz.exception.*;
import com.elearning.quiz.mapper.QuizMapper;
import com.elearning.quiz.repository.ChoiceRepository;
import com.elearning.quiz.repository.QuestionRepository;
import com.elearning.quiz.repository.QuizRepository;
import com.elearning.quiz.repository.SubmissionRepository;
import com.elearning.quiz.security.AuthenticatedUser;
import com.elearning.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizServiceImpl implements QuizService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final SubmissionRepository submissionRepository;
    private final CourseServiceClient courseServiceClient;
    private final EnrollmentServiceClient enrollmentServiceClient;
    private final QuizMapper quizMapper;

    @Override
    @Transactional
    public QuizResponse createQuiz(Long courseId, QuizRequest request, AuthenticatedUser currentUser) {
        assertOwnsCourseOrAdmin(courseId, currentUser);

        Quiz quiz = Quiz.builder()
                .courseId(courseId)
                .title(request.getTitle())
                .description(request.getDescription())
                .passingScorePercent(request.getPassingScorePercent())
                .build();

        return quizMapper.toResponse(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public QuizResponse updateQuiz(Long quizId, QuizRequest request, AuthenticatedUser currentUser) {
        Quiz quiz = getQuizOrThrow(quizId);
        assertOwnsCourseOrAdmin(quiz.getCourseId(), currentUser);

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setPassingScorePercent(request.getPassingScorePercent());

        return quizMapper.toResponse(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId, AuthenticatedUser currentUser) {
        Quiz quiz = getQuizOrThrow(quizId);
        assertOwnsCourseOrAdmin(quiz.getCourseId(), currentUser);
        quizRepository.delete(quiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizResponse> listQuizzesForCourse(Long courseId, AuthenticatedUser currentUser, String bearerToken) {
        CourseInternalDto course = courseServiceClient.getCourseById(courseId);
        boolean isOwner = course.instructorId().equals(currentUser.userId());
        boolean isAdmin = currentUser.hasRole(ROLE_ADMIN);

        if (!isOwner && !isAdmin && !enrollmentServiceClient.isEnrolled(courseId, bearerToken)) {
            throw new ForbiddenOperationException("Enroll in this course to see its quizzes");
        }

        return quizRepository.findByCourseId(courseId).stream()
                .map(quizMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDetailResponse getQuizDetail(Long quizId, AuthenticatedUser currentUser) {
        Quiz quiz = getQuizOrThrow(quizId);
        assertOwnsCourseOrAdmin(quiz.getCourseId(), currentUser);
        return quizMapper.toDetailResponse(quiz);
    }

    @Override
    @Transactional
    public QuestionResponse addQuestion(Long quizId, QuestionRequest request, AuthenticatedUser currentUser) {
        Quiz quiz = getQuizOrThrow(quizId);
        assertOwnsCourseOrAdmin(quiz.getCourseId(), currentUser);
        assertExactlyOneCorrectChoice(request);

        Question question = Question.builder()
                .quiz(quiz)
                .text(request.getText())
                .position(request.getPosition())
                .points(request.getPoints())
                .build();

        for (ChoiceRequest cr : request.getChoices()) {
            question.getChoices().add(Choice.builder()
                    .question(question)
                    .text(cr.getText())
                    .position(cr.getPosition())
                    .correct(cr.isCorrect())
                    .build());
        }

        return quizMapper.toQuestionResponse(questionRepository.save(question));
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, QuestionRequest request, AuthenticatedUser currentUser) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        assertOwnsCourseOrAdmin(question.getQuiz().getCourseId(), currentUser);
        assertExactlyOneCorrectChoice(request);

        question.setText(request.getText());
        question.setPosition(request.getPosition());
        question.setPoints(request.getPoints());

        // simplest correct way to handle "replace the choice set": clear and
        // rebuild rather than trying to diff/match existing choices by id
        question.getChoices().clear();
        for (ChoiceRequest cr : request.getChoices()) {
            question.getChoices().add(Choice.builder()
                    .question(question)
                    .text(cr.getText())
                    .position(cr.getPosition())
                    .correct(cr.isCorrect())
                    .build());
        }

        return quizMapper.toQuestionResponse(question);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId, AuthenticatedUser currentUser) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + questionId));
        assertOwnsCourseOrAdmin(question.getQuiz().getCourseId(), currentUser);
        questionRepository.delete(question);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizTakeResponse getQuizToTake(Long quizId, AuthenticatedUser currentUser, String bearerToken) {
        Quiz quiz = getQuizOrThrow(quizId);
        assertEnrolled(quiz.getCourseId(), bearerToken);
        return quizMapper.toTakeResponse(quiz);
    }

    @Override
    @Transactional
    public SubmissionDetailResponse submitQuiz(
            Long quizId, SubmitQuizRequest request, AuthenticatedUser currentUser, String bearerToken) {

        Quiz quiz = getQuizOrThrow(quizId);
        assertEnrolled(quiz.getCourseId(), bearerToken);

        Map<Long, Question> questionsById = new HashMap<>();
        for (Question q : quiz.getQuestions()) {
            questionsById.put(q.getId(), q);
        }

        int totalPoints = quiz.totalPoints();
        int earnedPoints = 0;

        Submission submission = Submission.builder()
                .quiz(quiz)
                .studentId(currentUser.userId())
                .attemptNumber((int) submissionRepository.countByQuizIdAndStudentId(quizId, currentUser.userId()) + 1)
                .build();

        List<AnswerResultResponse> results = new java.util.ArrayList<>();

        for (AnswerRequest answer : request.getAnswers()) {
            Question question = questionsById.get(answer.getQuestionId());
            if (question == null) {
                throw new InvalidSubmissionException("Question " + answer.getQuestionId() + " does not belong to this quiz");
            }

            Long correctChoiceId = question.correctChoiceId();
            boolean correct = answer.getChoiceId() != null && answer.getChoiceId().equals(correctChoiceId);
            if (correct) {
                earnedPoints += question.getPoints();
            }

            Choice selectedChoice = answer.getChoiceId() != null
                    ? choiceRepository.findById(answer.getChoiceId()).orElse(null)
                    : null;

            SubmissionAnswer submissionAnswer = SubmissionAnswer.builder()
                    .submission(submission)
                    .question(question)
                    .selectedChoice(selectedChoice)
                    .build();
            submission.getAnswers().add(submissionAnswer);

            results.add(AnswerResultResponse.builder()
                    .questionId(question.getId())
                    .selectedChoiceId(answer.getChoiceId())
                    .correctChoiceId(correctChoiceId)
                    .correct(correct)
                    .build());
        }

        int scorePercent = totalPoints == 0 ? 0 : (int) Math.round((earnedPoints * 100.0) / totalPoints);
        submission.setScorePercent(scorePercent);
        submission.setPassed(scorePercent >= quiz.getPassingScorePercent());

        Submission saved = submissionRepository.save(submission);
        log.info("Student {} scored {}% on quiz {} (attempt {})",
                currentUser.userId(), scorePercent, quizId, saved.getAttemptNumber());
        // TODO: publish a "QuizPassed" event once notification-service exists.

        return SubmissionDetailResponse.builder()
                .submission(quizMapper.toSubmissionResponse(saved))
                .answers(results)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> listMySubmissions(Long quizId, AuthenticatedUser currentUser) {
        return submissionRepository.findByQuizIdAndStudentIdOrderBySubmittedAtDesc(quizId, currentUser.userId()).stream()
                .map(quizMapper::toSubmissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubmissionResponse> listSubmissionsForQuiz(Long quizId, AuthenticatedUser currentUser) {
        Quiz quiz = getQuizOrThrow(quizId);
        assertOwnsCourseOrAdmin(quiz.getCourseId(), currentUser);

        return submissionRepository.findByQuizIdOrderBySubmittedAtDesc(quizId).stream()
                .map(quizMapper::toSubmissionResponse)
                .toList();
    }

    // ---- helpers ----

    private Quiz getQuizOrThrow(Long quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));
    }

    private void assertOwnsCourseOrAdmin(Long courseId, AuthenticatedUser currentUser) {
        if (currentUser.hasRole(ROLE_ADMIN)) {
            return;
        }
        CourseInternalDto course = courseServiceClient.getCourseById(courseId);
        if (!course.instructorId().equals(currentUser.userId())) {
            throw new ForbiddenOperationException("You do not have permission to manage this course's quizzes");
        }
    }

    private void assertEnrolled(Long courseId, String bearerToken) {
        if (!enrollmentServiceClient.isEnrolled(courseId, bearerToken)) {
            throw new NotEnrolledException("You must be enrolled in this course to take its quizzes");
        }
    }

    private void assertExactlyOneCorrectChoice(QuestionRequest request) {
        long correctCount = request.getChoices().stream().filter(ChoiceRequest::isCorrect).count();
        if (correctCount != 1) {
            throw new InvalidSubmissionException("A question must have exactly one correct choice");
        }
    }
}
