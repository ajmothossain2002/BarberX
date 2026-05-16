package com.barberx.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends CustomException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
