package com.elearning.enrollment.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final CourseServiceProperties courseServiceProperties;

    /**
     * Direct HTTP call to course-service, hardcoded to its configured base URL.
     * Once discovery-server (Eureka) exists, this should become a load-balanced
     * client resolving "course-service" by name instead of a fixed URL — this is
     * the seam where that change happens later.
     */
    @Bean
    public RestClient courseServiceRestClient() {
        return RestClient.builder()
                .baseUrl(courseServiceProperties.getBaseUrl())
                .build();
    }
}
