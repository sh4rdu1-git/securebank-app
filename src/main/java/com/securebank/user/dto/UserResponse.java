package com.securebank.user.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO for outgoing user data in API responses.
 *
 * Notice what is NOT here:
 * - passwordHash: never exposed, not even as "hidden"
 * - version: internal JPA optimistic lock counter, irrelevant to clients
 * - nationalId: sensitive PII, not returned after registration
 *
 * Only expose what the client genuinely needs to function.
 * This principle — "data minimisation" — is both a security best practice
 * and a legal requirement under regulations like GDPR.
 */
@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
    private boolean active;
    private ZonedDateTime createdAt;
}
