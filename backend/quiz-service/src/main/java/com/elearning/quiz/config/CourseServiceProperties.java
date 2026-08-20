package com.elearning.quiz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.course-service")
@Getter
@Setter
public class CourseServiceProperties {
    private String baseUrl;
}
