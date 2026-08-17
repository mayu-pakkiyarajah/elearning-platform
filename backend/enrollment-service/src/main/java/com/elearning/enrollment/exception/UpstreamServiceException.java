package com.elearning.enrollment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a call to another microservice (course-service) fails or times out. */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }
}
