package com.elearning.enrollment;

import com.elearning.enrollment.client.CourseInternalDto;
import com.elearning.enrollment.client.CourseServiceClient;
import com.elearning.enrollment.dto.request.EnrollRequest;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * course-service is mocked at the CourseServiceClient bean level (not a real HTTP
 * call) — enrollment-service's own logic is what's under test here, not the
 * network hop to another service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EnrollmentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseServiceClient courseServiceClient;

    private static final long PUBLISHED_COURSE_ID = 100L;
    private static final long DRAFT_COURSE_ID = 200L;

    @BeforeEach
    void setUp() {
        when(courseServiceClient.getCourseById(eq(PUBLISHED_COURSE_ID))).thenReturn(
                new CourseInternalDto(PUBLISHED_COURSE_ID, "Intro to Spring Boot", "intro-to-spring-boot", "PUBLISHED", 999L, 2)
        );
        when(courseServiceClient.getCourseById(eq(DRAFT_COURSE_ID))).thenReturn(
                new CourseInternalDto(DRAFT_COURSE_ID, "Unfinished Course", "unfinished-course", "DRAFT", 999L, 0)
        );
    }

    @Test
    void studentCanEnrollAndCompleteAllLessons() throws Exception {
        String studentToken = TestJwtFactory.token(1L, "student@example.com", "ROLE_STUDENT");

        // enroll
        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollRequest(PUBLISHED_COURSE_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(PUBLISHED_COURSE_ID))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.progressPercent").value(0));

        // re-enrolling is idempotent, not a conflict
        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollRequest(PUBLISHED_COURSE_ID))))
                .andExpect(status().isOk());

        // complete lesson 1 of 2
        mockMvc.perform(post("/api/v1/enrollments/mine/{courseId}/lessons/{lessonId}/complete", PUBLISHED_COURSE_ID, 1L)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollment.status").value("ACTIVE"))
                .andExpect(jsonPath("$.enrollment.progressPercent").value(50));

        // complete lesson 2 of 2 — should auto-complete the course
        mockMvc.perform(post("/api/v1/enrollments/mine/{courseId}/lessons/{lessonId}/complete", PUBLISHED_COURSE_ID, 2L)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollment.status").value("COMPLETED"))
                .andExpect(jsonPath("$.enrollment.progressPercent").value(100))
                .andExpect(jsonPath("$.completedLessonIds.length()").value(2));
    }

    @Test
    void cannotEnrollInDraftCourse() throws Exception {
        String studentToken = TestJwtFactory.token(2L, "student2@example.com", "ROLE_STUDENT");

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollRequest(DRAFT_COURSE_ID))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void instructorCanViewRoster_butOnlyForOwnCourse() throws Exception {
        String ownerToken = TestJwtFactory.token(999L, "owner@example.com", "ROLE_INSTRUCTOR");
        String otherInstructorToken = TestJwtFactory.token(555L, "other@example.com", "ROLE_INSTRUCTOR");
        String studentToken = TestJwtFactory.token(3L, "student3@example.com", "ROLE_STUDENT");

        mockMvc.perform(post("/api/v1/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new EnrollRequest(PUBLISHED_COURSE_ID))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/enrollments/course/{courseId}", PUBLISHED_COURSE_ID)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/enrollments/course/{courseId}", PUBLISHED_COURSE_ID)
                        .header("Authorization", "Bearer " + otherInstructorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotViewRoster() throws Exception {
        String studentToken = TestJwtFactory.token(4L, "student4@example.com", "ROLE_STUDENT");

        mockMvc.perform(get("/api/v1/enrollments/course/{courseId}", PUBLISHED_COURSE_ID)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
