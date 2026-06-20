package com.securebank.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securebank.common.exception.ConflictException;
import com.securebank.common.exception.ResourceNotFoundException;
import com.securebank.user.dto.RegistrationRequest;
import com.securebank.user.dto.UserResponse;
import com.securebank.user.entity.User;
import com.securebank.user.entity.UserRole;
import com.securebank.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * UserService contains all business logic related to user management.
 *
 * Layering rule: this class knows about the database (via UserRepository)
 * and about business rules, but knows nothing about HTTP. It does not
 * use HttpServletRequest, ResponseEntity, or any web layer concept.
 * This separation means the service can be called from a REST controller,
 * a scheduled job, a message consumer, or a test — without any changes.
 *
 * @Transactional at method level:
 *                Each public method that modifies data has its own transaction
 *                boundary.
 *                readOnly = true is a performance optimisation for read methods
 *                —
 *                Hibernate skips dirty checking (scanning all loaded entities
 *                for changes),
 *                which is unnecessary overhead when you are only reading.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(RegistrationRequest request) {

        log.info("Registration attempt for email: {}", request.getEmail());

        // Check uniqueness before attempting to save.
        // Checking first gives a clean, human-readable error message.
        // Relying on DB constraint violations gives an ugly, technical error.
        // Both prevent duplicates, but only one is professional.
        if (userRepository.existsByEmail(
                request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("An account with this email already exists.");
        }
        if (userRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {
            throw new ConflictException("An account with this phone number already exists.");
        }
        if (userRepository.existsByNationalId(
                request.getNationalId())) {
            throw new ConflictException("An account with this national ID already exists.");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                // Normalise email to lowercase before storing.
                // "User@Example.COM" and "user@example.com" should be the same account.
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                // BCrypt hashing happens here. The plain-text password from the
                // request is used exactly once — to produce the hash — and is
                // then discarded. It is NEVER logged, stored, or passed further.
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phoneNumber(request.getPhoneNumber())
                .nationalId(request.getNationalId())
                .role(UserRole.ROLE_CUSTOMER)
                // Role is ALWAYS set server-side. Never trust the client to set roles.
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully. ID: {}", savedUser.getId());
        // Log the ID (non-sensitive) for traceability.
        // Never log the email, name, or any PII in production.

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    // Private helper — keeps the mapping logic in one place.
    // If UserResponse fields change, you update this method only.
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
