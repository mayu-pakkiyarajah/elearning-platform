package com.elearning.quiz.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Direct HTTP calls to course-service and enrollment-service, hardcoded to their
 * configured base URLs. Once discovery-server (Eureka) exists, these become
 * load-balanced clients resolving each service by name instead of a fixed URL.
 */
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final CourseServiceProperties courseServiceProperties;
    private final EnrollmentServiceProperties enrollmentServiceProperties;

    @Bean
    public RestClient courseServiceRestClient() {
        return RestClient.builder().baseUrl(courseServiceProperties.getBaseUrl()).build();
    }

    @Bean
    public RestClient enrollmentServiceRestClient() {
        return RestClient.builder().baseUrl(enrollmentServiceProperties.getBaseUrl()).build();
    }
}
