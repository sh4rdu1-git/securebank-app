package com.securebank.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource does not exist in the database.
 * Examples: user not found by ID, account number does not exist.
 *
 * @ResponseStatus is a hint to Spring MVC about the HTTP status code.
 *                 However, since we have a GlobalExceptionHandler, that takes
 *                 precedence.
 *                 We keep @ResponseStatus here for documentation clarity — any
 *                 developer
 *                 reading this class immediately knows it maps to a 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(
            String resourceName,
            String fieldName,
            Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
