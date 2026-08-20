package com.elearning.quiz.client;

import com.elearning.quiz.exception.ResourceNotFoundException;
import com.elearning.quiz.exception.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseServiceClient {

    private final RestClient courseServiceRestClient;

    public CourseInternalDto getCourseById(Long courseId) {
        try {
            return courseServiceRestClient.get()
                    .uri("/api/v1/courses/id/{id}", courseId)
                    .retrieve()
                    .body(CourseInternalDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        } catch (RestClientException ex) {
            log.error("course-service call failed for course {}: {}", courseId, ex.getMessage());
            throw new UpstreamServiceException("Could not reach course-service — please try again shortly");
        }
    }
}
