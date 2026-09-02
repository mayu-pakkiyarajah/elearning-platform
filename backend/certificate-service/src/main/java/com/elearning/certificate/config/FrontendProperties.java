package com.elearning.certificate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.frontend")
@Getter
@Setter
public class FrontendProperties {
    private String baseUrl;
}
