package com.elearning.quiz.client;

import com.elearning.quiz.exception.UpstreamServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * enrollment-service's GET /enrollments/mine/{courseId} answers "is *the caller*
 * enrolled" based on the JWT it receives — there's no "check enrollment for
 * arbitrary student X" endpoint (and there shouldn't be one without its own auth
 * story). So this client forwards the original request's Bearer token rather than
 * minting its own service-to-service credential. That's a deliberate simplification:
 * a proper service-to-service call (e.g. for a background job) would need its own
 * auth mechanism, which doesn't exist yet in this platform.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentServiceClient {

    private final RestClient enrollmentServiceRestClient;

    public boolean isEnrolled(Long courseId, String bearerToken) {
        try {
            enrollmentServiceRestClient.get()
                    .uri("/api/v1/enrollments/mine/{courseId}", courseId)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound ex) {
            return false;
        } catch (RestClientException ex) {
            log.error("enrollment-service call failed for course {}: {}", courseId, ex.getMessage());
            throw new UpstreamServiceException("Could not verify enrollment — please try again shortly");
        }
    }
}
