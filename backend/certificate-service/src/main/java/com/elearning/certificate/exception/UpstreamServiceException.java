package com.elearning.certificate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }
}
