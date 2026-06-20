package com.securebank.user.controller;

import com.securebank.common.dto.ApiResponse;
import com.securebank.user.dto.RegistrationRequest;
import com.securebank.user.dto.UserResponse;
import com.securebank.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for authentication-related endpoints.
 *
 * Controller responsibilities (and ONLY these):
 * 1. Receive the HTTP request
 * 2. Validate the request format (@Valid triggers Bean Validation)
 * 3. Call the appropriate service method
 * 4. Wrap the result in an ApiResponse
 * 5. Return the appropriate HTTP status code
 *
 * The controller has zero business logic. It does not check if an email
 * exists, does not hash passwords, does not touch the database directly.
 * Those concerns belong in the service layer.
 *
 * @RequestMapping("/api/v1/auth") — note the versioning.
 * Versioning your API from day one (/api/v1/) means you can introduce
 * /api/v2/ later with breaking changes without disrupting existing clients.
 * This is standard practice for any public or long-lived API.
 */

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    public final UserService userService;

    /**
     * POST /api/v1/auth/register
     *
     * @Valid triggers Bean Validation on the RegistrationRequest.
     *        If any constraint fails, Spring calls GlobalExceptionHandler
     *        before this method body executes at all.
     *
     *        Returns HTTP 201 Created (not 200 OK) because a new resource
     *        was created. Using the correct HTTP status codes makes your API
     *        self-documenting and compatible with REST conventions.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegistrationRequest request) {

        log.debug("Registration request received for email: {}", request.getEmail());

        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        response,
                        "Registration successful. Welcome to SecureBank.",
                        201));
    }
}
