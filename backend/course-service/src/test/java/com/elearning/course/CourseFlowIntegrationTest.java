package com.elearning.course;

import com.elearning.course.dto.request.CourseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseRequest sampleCourse(String title) {
        return CourseRequest.builder()
                .title(title)
                .subtitle("Learn the basics")
                .description("A complete beginner course")
                .categoryId(null)
                .level("BEGINNER")
                .language("English")
                .price(new BigDecimal("29.99"))
                .thumbnailUrl(null)
                .build();
    }

    @Test
    void instructorCanCreateCourse_andItAppearsAfterPublishing() throws Exception {
        String instructorToken = TestJwtFactory.token(1L, "instructor@example.com", "ROLE_INSTRUCTOR");

        String response = mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + instructorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCourse("Intro to Spring Boot"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();

        Long courseId = objectMapper.readTree(response).get("id").asLong();

        // draft courses shouldn't show up in the public catalog yet
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(patch("/api/v1/courses/{id}/publish", courseId)
                        .header("Authorization", "Bearer " + instructorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Intro to Spring Boot"));
    }

    @Test
    void studentCannotCreateCourse() throws Exception {
        String studentToken = TestJwtFactory.token(2L, "student@example.com", "ROLE_STUDENT");

        mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCourse("Should Not Be Created"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void instructorCannotEditAnotherInstructorsCourse() throws Exception {
        String ownerToken = TestJwtFactory.token(10L, "owner@example.com", "ROLE_INSTRUCTOR");
        String otherToken = TestJwtFactory.token(20L, "other@example.com", "ROLE_INSTRUCTOR");

        String response = mockMvc.perform(post("/api/v1/courses")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCourse("Owner's Course"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long courseId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/v1/courses/{id}", courseId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCourse("Hijacked Title"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserCanBrowseButNotCreate() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCourse("Anonymous Course"))))
                .andExpect(status().isUnauthorized());
    }
}
