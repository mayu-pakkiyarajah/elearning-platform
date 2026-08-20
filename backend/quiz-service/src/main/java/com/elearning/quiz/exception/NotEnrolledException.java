package com.elearning.quiz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotEnrolledException extends RuntimeException {
    public NotEnrolledException(String message) {
        super(message);
    }
}
