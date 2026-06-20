package com.securebank.common.dto;

import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

/**
 * Every API response from SecureBank — success or failure — is wrapped
 * in this envelope. This is called the "consistent API response pattern."
 *
 * Why does this matter?
 * Without this, your API might return:
 * - { "id": 1, "email": "x" } on success
 * - { "error": "not found" } on failure
 * - { "message": "conflict" } on another failure
 *
 * Clients (mobile apps, frontends, other services) cannot write reliable
 * error-handling code against an inconsistent API. With this envelope,
 * every response has the same structure and clients always know
 * where to look for data, errors, and status.
 *
 * @JsonInclude(NON_NULL): fields that are null are omitted from the JSON
 * output entirely. A success response won't include an "errors" key at all.
 * This keeps responses clean and unambiguous.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String message;
    private final T data;
    private final Map<String, String> errors;
    private final int statusCode;

    @Builder.Default
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    // ----------------------------------------------------------------
    // Static factory methods — clean, readable call sites:
    // return ApiResponse.success(userResponse, "User registered successfully");
    // return ApiResponse.error("Email already exists", 409);
    // ----------------------------------------------------------------

    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .statusCode(statusCode)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .build();
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .statusCode(400)
                .build();
    }
}
