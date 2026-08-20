package com.elearning.quiz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.enrollment-service")
@Getter
@Setter
public class EnrollmentServiceProperties {
    private String baseUrl;
}
