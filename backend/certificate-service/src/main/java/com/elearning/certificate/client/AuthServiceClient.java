package com.elearning.certificate.client;

import com.elearning.certificate.exception.ResourceNotFoundException;
import com.elearning.certificate.exception.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestClient authServiceRestClient;

    /**
     * auth-service requires a valid JWT for this lookup — names/emails are PII, so
     * it's not a public endpoint like course-service's course-by-id. We forward the
     * calling student's own Bearer token (they're generating their own certificate,
     * so this is really "give me my own name back"), same pattern as
     * EnrollmentServiceClient.
     */
    public UserInternalDto getUserById(Long userId, String bearerToken) {
        try {
            return authServiceRestClient.get()
                    .uri("/api/v1/users/id/{id}", userId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .body(UserInternalDto.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("User not found: " + userId);
        } catch (RestClientException ex) {
            log.error("auth-service call failed for user {}: {}", userId, ex.getMessage());
            throw new UpstreamServiceException("Could not reach auth-service — please try again shortly");
        }
    }
}
