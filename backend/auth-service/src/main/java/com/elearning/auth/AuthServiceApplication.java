package com.elearning.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Note: JwtProperties / CorsProperties / PasswordResetProperties are already
// annotated with @Component + @ConfigurationProperties, so plain component
// scan picks them up — no need for @ConfigurationPropertiesScan as well.
@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
