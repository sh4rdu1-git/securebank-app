package com.securebank.user.service;

import com.securebank.common.exception.ConflictException;
import com.securebank.user.dto.RegistrationRequest;
import com.securebank.user.dto.UserResponse;
import com.securebank.user.entity.User;
import com.securebank.user.entity.UserRole;
import com.securebank.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service - Unit Test")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RegistrationRequest.builder()
                .firstName("Arjun")
                .lastName("Sharma")
                .email("arjun.sharma@example.com")
                .password("SecurePass1!")
                .phoneNumber("+919876543210")
                .nationalId("ABCDE1234F")
                .build();
    }

    @Test
    @DisplayName("Should register user successfully when all inputs are unique")
    void shouldRegisterUserSuccessfully() {
        // GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userRepository.existsByNationalId(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashedpassword");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("arjun.sharma@example.com")
                .firstName("Arjun")
                .lastName("Sharma")
                .phoneNumber("+919876543210")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .createdAt(ZonedDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // WHEN
        UserResponse response = userService.registerUser(validRequest);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("arjun.sharma@example.com");
        assertThat(response.getRole()).isEqualTo("ROLE_CUSTOMER");

        // Critical assertion: password was hashed before saving
        verify(passwordEncoder).encode("SecurePass1!");
        verify(userRepository).save(argThat(
                user -> user.getPasswordHash().equals("$2a$12$hashedpassword") &&
                        user.getRole() == UserRole.ROLE_CUSTOMER));
    }

    @Test
    @DisplayName("Should throw ConflictException when email already exists")
    void shouldThrowConflictExceptionForDuplicateEmail() {
        // GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("email");

        // Verify we short-circuit and never attempt to save
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        // Password should never be hashed if we're going to reject anyway —
        // BCrypt is intentionally slow, so never run it unnecessarily
    }

    @Test
    @DisplayName("Should throw ConflictException when phone number already exists")
    void shouldThrowConflictExceptionForDuplicatePhone() {
        // GIVEN
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        // WHEN & THEN
        assertThatThrownBy(() -> userService.registerUser(validRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("phone");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should normalise email to lowercase before saving")
    void shouldNormaliseEmailToLowercase() {
        // GIVEN — email with mixed case
        validRequest.setEmail("Arjun.SHARMA@Example.COM");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userRepository.existsByNationalId(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("arjun.sharma@example.com")
                .firstName("Arjun")
                .lastName("Sharma")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .createdAt(ZonedDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // WHEN
        userService.registerUser(validRequest);

        // THEN — verify the saved entity has lowercase email
        verify(userRepository).save(argThat(user -> user.getEmail().equals("arjun.sharma@example.com")));
    }

    @Test
    @DisplayName("Should never set role from request — always force ROLE_CUSTOMER")
    void shouldAlwaysForceCustomerRole() {
        // This test protects against privilege escalation.
        // Even if somehow a ROLE_ADMIN value reached the service,
        // it must be overridden to ROLE_CUSTOMER.
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userRepository.existsByNationalId(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hash");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .email("arjun.sharma@example.com")
                .firstName("Arjun")
                .lastName("Sharma")
                .role(UserRole.ROLE_CUSTOMER)
                .active(true)
                .createdAt(ZonedDateTime.now())
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.registerUser(validRequest);

        verify(userRepository).save(argThat(user -> user.getRole() == UserRole.ROLE_CUSTOMER));
    }
}
