package com.elearning.certificate;

import com.elearning.certificate.client.AuthServiceClient;
import com.elearning.certificate.client.EnrollmentDetailDto;
import com.elearning.certificate.client.EnrollmentServiceClient;
import com.elearning.certificate.client.EnrollmentSummaryDto;
import com.elearning.certificate.client.UserInternalDto;
import com.elearning.certificate.dto.request.GenerateCertificateRequest;
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

/** auth-service and enrollment-service are mocked at the client-bean level —
 *  certificate-service's own eligibility/authorization/PDF logic is under test. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CertificateFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthServiceClient authServiceClient;

    @MockBean
    private EnrollmentServiceClient enrollmentServiceClient;

    private static final long COMPLETED_COURSE_ID = 1L;
    private static final long ACTIVE_COURSE_ID = 2L;
    private static final long STUDENT_ID = 10L;

    @BeforeEach
    void setUp() {
        when(authServiceClient.getUserById(eq(STUDENT_ID), anyString()))
                .thenReturn(new UserInternalDto(STUDENT_ID, "Ada", "Lovelace", "ada@example.com"));

        when(enrollmentServiceClient.getEnrollmentDetail(eq(COMPLETED_COURSE_ID), anyString()))
                .thenReturn(new EnrollmentDetailDto(
                        new EnrollmentSummaryDto(1L, COMPLETED_COURSE_ID, "Intro to Spring Boot", "intro-to-spring-boot",
                                "COMPLETED", 5, 5, 100, "2026-01-01T00:00:00", "2026-01-05T00:00:00"),
                        List.of(1L, 2L, 3L, 4L, 5L)
                ));

        when(enrollmentServiceClient.getEnrollmentDetail(eq(ACTIVE_COURSE_ID), anyString()))
                .thenReturn(new EnrollmentDetailDto(
                        new EnrollmentSummaryDto(2L, ACTIVE_COURSE_ID, "Advanced Kubernetes", "advanced-kubernetes",
                                "ACTIVE", 8, 3, 38, "2026-01-01T00:00:00", null),
                        List.of(1L, 2L, 3L)
                ));
    }

    @Test
    void studentCanGenerateAndDownloadCertificateForCompletedCourse() throws Exception {
        String token = TestJwtFactory.token(STUDENT_ID, "ada@example.com", "ROLE_STUDENT");

        String response = mockMvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateCertificateRequest(COMPLETED_COURSE_ID))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentName").value("Ada Lovelace"))
                .andExpect(jsonPath("$.courseTitle").value("Intro to Spring Boot"))
                .andExpect(jsonPath("$.verificationCode").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Long certificateId = objectMapper.readTree(response).get("id").asLong();
        String code = objectMapper.readTree(response).get("verificationCode").asText();

        byte[] pdfBytes = mockMvc.perform(get("/api/v1/certificates/{id}/download", certificateId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andReturn().getResponse().getContentAsByteArray();

        assertPdfMagicBytes(pdfBytes);

        // public verification — no Authorization header at all
        mockMvc.perform(get("/api/v1/certificates/verify/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.studentName").value("Ada Lovelace"));
    }

    @Test
    void generatingTwiceIsIdempotent() throws Exception {
        String token = TestJwtFactory.token(STUDENT_ID, "ada@example.com", "ROLE_STUDENT");
        GenerateCertificateRequest request = new GenerateCertificateRequest(COMPLETED_COURSE_ID);

        String first = mockMvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String second = mockMvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Long firstId = objectMapper.readTree(first).get("id").asLong();
        Long secondId = objectMapper.readTree(second).get("id").asLong();
        org.junit.jupiter.api.Assertions.assertEquals(firstId, secondId);
    }

    @Test
    void cannotGenerateCertificateForIncompleteCourse() throws Exception {
        String token = TestJwtFactory.token(STUDENT_ID, "ada@example.com", "ROLE_STUDENT");

        mockMvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateCertificateRequest(ACTIVE_COURSE_ID))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void studentCannotDownloadAnotherStudentsCertificate() throws Exception {
        String ownerToken = TestJwtFactory.token(STUDENT_ID, "ada@example.com", "ROLE_STUDENT");
        String otherToken = TestJwtFactory.token(99L, "other@example.com", "ROLE_STUDENT");
        when(authServiceClient.getUserById(eq(99L), anyString()))
                .thenReturn(new UserInternalDto(99L, "Grace", "Hopper", "grace@example.com"));

        String response = mockMvc.perform(post("/api/v1/certificates")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GenerateCertificateRequest(COMPLETED_COURSE_ID))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long certificateId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/v1/certificates/{id}/download", certificateId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void verifyingAnUnknownCodeReturnsInvalidNotError() throws Exception {
        mockMvc.perform(get("/api/v1/certificates/verify/{code}", "BOGUSCODE1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    private void assertPdfMagicBytes(byte[] bytes) {
        String header = new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
        org.junit.jupiter.api.Assertions.assertEquals("%PDF", header);
    }
}
