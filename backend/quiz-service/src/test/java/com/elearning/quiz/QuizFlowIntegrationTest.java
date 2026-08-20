package com.elearning.quiz;

import com.elearning.quiz.client.CourseInternalDto;
import com.elearning.quiz.client.CourseServiceClient;
import com.elearning.quiz.client.EnrollmentServiceClient;
import com.elearning.quiz.dto.request.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** course-service and enrollment-service are mocked at the client-bean level —
 *  quiz-service's own scoring/authorization logic is what's under test. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseServiceClient courseServiceClient;

    @MockBean
    private EnrollmentServiceClient enrollmentServiceClient;

    private static final long COURSE_ID = 500L;
    private static final long INSTRUCTOR_ID = 900L;

    @BeforeEach
    void setUp() {
        when(courseServiceClient.getCourseById(eq(COURSE_ID))).thenReturn(
                new CourseInternalDto(COURSE_ID, "Java Basics", "java-basics", "PUBLISHED", INSTRUCTOR_ID, 5)
        );
    }

    private QuestionRequest twoPlusTwoQuestion() {
        return QuestionRequest.builder()
                .text("What is 2 + 2?")
                .position(1)
                .points(1)
                .choices(List.of(
                        ChoiceRequest.builder().text("3").position(1).correct(false).build(),
                        ChoiceRequest.builder().text("4").position(2).correct(true).build(),
                        ChoiceRequest.builder().text("5").position(3).correct(false).build()
                ))
                .build();
    }

    @Test
    void instructorCanBuildQuiz_studentCanTakeAndGetScored() throws Exception {
        String instructorToken = TestJwtFactory.token(INSTRUCTOR_ID, "instructor@example.com", "ROLE_INSTRUCTOR");
        String studentToken = TestJwtFactory.token(1L, "student@example.com", "ROLE_STUDENT");

        when(enrollmentServiceClient.isEnrolled(eq(COURSE_ID), anyString())).thenReturn(true);

        // create quiz
        String quizResponse = mockMvc.perform(post("/api/v1/courses/{courseId}/quizzes", COURSE_ID)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                QuizRequest.builder().title("Chapter 1 Quiz").description("Basics").passingScorePercent(50).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quizId = objectMapper.readTree(quizResponse).get("id").asLong();

        // add a question
        String questionResponse = mockMvc.perform(post("/api/v1/quizzes/{quizId}/questions", quizId)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(twoPlusTwoQuestion())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long questionId = objectMapper.readTree(questionResponse).get("id").asLong();
        Long correctChoiceId = null;
        for (var choiceNode : objectMapper.readTree(questionResponse).get("choices")) {
            if (choiceNode.get("correct").asBoolean()) {
                correctChoiceId = choiceNode.get("id").asLong();
            }
        }

        // student takes the quiz — correct answers must NOT be present
        mockMvc.perform(get("/api/v1/quizzes/{id}/take", quizId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].choices[0].correct").doesNotExist());

        // submit correct answer
        SubmitQuizRequest submitRequest = SubmitQuizRequest.builder()
                .answers(List.of(AnswerRequest.builder().questionId(questionId).choiceId(correctChoiceId).build()))
                .build();

        mockMvc.perform(post("/api/v1/quizzes/{id}/submit", quizId)
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submission.scorePercent").value(100))
                .andExpect(jsonPath("$.submission.passed").value(true))
                .andExpect(jsonPath("$.answers[0].correct").value(true));
    }

    @Test
    void nonEnrolledStudentCannotTakeQuiz() throws Exception {
        String instructorToken = TestJwtFactory.token(INSTRUCTOR_ID, "instructor2@example.com", "ROLE_INSTRUCTOR");
        String studentToken = TestJwtFactory.token(2L, "outsider@example.com", "ROLE_STUDENT");

        when(enrollmentServiceClient.isEnrolled(eq(COURSE_ID), anyString())).thenReturn(false);

        String quizResponse = mockMvc.perform(post("/api/v1/courses/{courseId}/quizzes", COURSE_ID)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                QuizRequest.builder().title("Locked Quiz").passingScorePercent(70).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quizId = objectMapper.readTree(quizResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/quizzes/{id}/take", quizId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonOwningInstructorCannotAddQuestions() throws Exception {
        String ownerToken = TestJwtFactory.token(INSTRUCTOR_ID, "owner@example.com", "ROLE_INSTRUCTOR");
        String otherToken = TestJwtFactory.token(777L, "other@example.com", "ROLE_INSTRUCTOR");

        String quizResponse = mockMvc.perform(post("/api/v1/courses/{courseId}/quizzes", COURSE_ID)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                QuizRequest.builder().title("Owner's Quiz").passingScorePercent(70).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quizId = objectMapper.readTree(quizResponse).get("id").asLong();

        mockMvc.perform(post("/api/v1/quizzes/{quizId}/questions", quizId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(twoPlusTwoQuestion())))
                .andExpect(status().isForbidden());
    }

    @Test
    void questionMustHaveExactlyOneCorrectChoice() throws Exception {
        String instructorToken = TestJwtFactory.token(INSTRUCTOR_ID, "instructor3@example.com", "ROLE_INSTRUCTOR");

        String quizResponse = mockMvc.perform(post("/api/v1/courses/{courseId}/quizzes", COURSE_ID)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                QuizRequest.builder().title("Bad Quiz").passingScorePercent(70).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long quizId = objectMapper.readTree(quizResponse).get("id").asLong();

        QuestionRequest badQuestion = QuestionRequest.builder()
                .text("Ambiguous question")
                .position(1)
                .points(1)
                .choices(List.of(
                        ChoiceRequest.builder().text("A").position(1).correct(true).build(),
                        ChoiceRequest.builder().text("B").position(2).correct(true).build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/quizzes/{quizId}/questions", quizId)
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badQuestion)))
                .andExpect(status().isBadRequest());
    }
}
