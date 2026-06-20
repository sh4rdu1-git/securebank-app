package com.securebank.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO (Data Transfer Object) for the registration request.
 *
 * Why a DTO instead of the User entity directly?
 *
 * If we accepted the User entity as the request body, a malicious caller
 * could POST:
 * { "email": "hacker@x.com", "role": "ROLE_ADMIN", "verified": true }
 * and Spring would map those fields directly onto a User entity,
 * potentially giving themselves admin access.
 *
 * The DTO is a strict contract — it only contains fields the client
 * is allowed to provide. Everything else (role, active status, timestamps)
 * is set server-side in the service layer, never from client input.
 *
 * The Bean Validation annotations (@NotBlank, @Email, @Pattern) are
 * checked automatically by Spring before the controller method runs,
 * triggered by @Valid in the controller. If any constraint fails,
 * Spring calls our GlobalExceptionHandler.handleValidationErrors() and
 * returns a 400 with the specific field errors. The service layer
 * never even sees an invalid request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name should be between 2 and 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name should be between 2 and 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be atleast 8 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Password must contain at least one uppercase letter, "
            + "one lowercase letter, one digit, and one special character (@#$%^&+=!)")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone number must be in international format (e.g. +919876543210)")
    private String phoneNumber;

    @NotBlank(message = "National ID is required")
    @Size(min = 5, max = 50, message = "National ID must be between 5 and 50 characters")
    private String nationalId;
}
