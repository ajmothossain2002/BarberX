package com.barberx.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Application-level custom exception carrying HTTP status and descriptive message.
 */
@Getter
public class CustomException extends RuntimeException {

    private final HttpStatus status;

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CustomException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
