package com.securebank.common.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.securebank.common.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Centralized exception handling for the entire application.
 *
 * Without this class, every controller method would need a try-catch block,
 * and exceptions that slip through would return Spring's default error page
 * (a white-label HTML error page or a raw stack trace — both terrible for an
 * API).
 *
 * With @RestControllerAdvice, Spring intercepts any unhandled exception thrown
 * anywhere in the controller or service layer and routes it to the matching
 * 
 * @ExceptionHandler method here. One place, one responsibility.
 *
 *                   Industry note: In microservices, this class is often
 *                   extracted into a shared
 *                   library so all services have consistent error responses
 *                   automatically.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation failures (@NotBlank, @Email, @Size, etc.)
     * Spring Boot triggers this when a @Valid annotated request body fails
     * validation before the controller method even executes.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError("Validation error. Please check your input.", errors));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), 404));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), 409));
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOperation(
            InvalidOperationException ex) {
        log.warn("Invalid operation: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.error(ex.getMessage(), 422));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            BadCredentialsException ex) {
        // Deliberately generic — do NOT reveal whether email or password was wrong.
        // "Invalid email" lets an attacker enumerate valid emails in your system.
        // "Invalid credentials" reveals nothing useful to an attacker.
        log.warn("Failed login attempt");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials", 401));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AuthorizationDeniedException ex) {
        // Again, deliberately vague. Don't tell the caller what permission they lack.
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied", 403));
    }

    /**
     * Catch-all handler. If an exception reaches here, it is unexpected.
     * We log the full stack trace (so engineers can investigate) but return
     * a generic message to the client (so we don't leak internal details).
     *
     * NEVER return stack traces, SQL errors, or internal class names to API
     * clients.
     * These are goldmines for attackers doing reconnaissance.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllOtherExceptions(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again later.", 500));
    }
}
