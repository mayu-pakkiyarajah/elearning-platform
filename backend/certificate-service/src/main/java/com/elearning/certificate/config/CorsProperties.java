package com.elearning.certificate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProperties {
    private String allowedOrigins;

    public List<String> getAllowedOriginsList() {
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
    }
}
