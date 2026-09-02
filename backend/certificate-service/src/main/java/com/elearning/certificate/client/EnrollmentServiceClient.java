package com.elearning.certificate.client;

import com.elearning.certificate.exception.CourseNotCompletedException;
import com.elearning.certificate.exception.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Like quiz-service's EnrollmentServiceClient, this forwards the caller's own
 * Bearer token — enrollment-service's /enrollments/mine/{courseId} only answers
 * "is *the calling user* enrolled/completed", there's no arbitrary-student lookup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceClient {

    private final RestClient enrollmentServiceRestClient;

    /** Returns the enrollment detail, or throws if the caller was never enrolled at all. */
    public EnrollmentDetailDto getEnrollmentDetail(Long courseId, String bearerToken) {
        try {
            return enrollmentServiceRestClient.get()
                    .uri("/api/v1/enrollments/mine/{courseId}", courseId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(EnrollmentDetailDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CourseNotCompletedException("You are not enrolled in this course");
        } catch (RestClientException ex) {
            log.error("enrollment-service call failed for course {}: {}", courseId, ex.getMessage());
            throw new UpstreamServiceException("Could not verify course completion — please try again shortly");
        }
    }
}
