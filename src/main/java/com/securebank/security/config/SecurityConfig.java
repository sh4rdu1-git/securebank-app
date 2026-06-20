package com.securebank.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security configuration for Phase 1 (registration only).
 *
 * This is intentionally incomplete — we are building incrementally.
 * The full JWT filter chain will be added in the next phase.
 *
 * What is configured here:
 * - CSRF disabled (correct for stateless REST APIs — explained in detail
 * when we build the full security layer)
 * - /api/v1/auth/** is publicly accessible (register, and later login)
 * - /actuator/health is publicly accessible (Kubernetes probes need this)
 * - Everything else requires authentication (fail-safe default)
 * - Session management is STATELESS (no server-side sessions — JWT handles
 * state)
 * - BCryptPasswordEncoder with strength 12 is defined as a Bean so it can
 * be injected into UserService via Spring's dependency injection
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        /**
         * BCrypt strength 12 means 2^12 = 4096 hashing rounds.
         * At this strength, hashing one password takes ~400ms on modern hardware.
         * This is intentional — it makes brute-force attacks computationally
         * expensive. An attacker trying 1 billion passwords per second on a
         * plain hash would take ~317 years on a BCrypt-12 hash.
         *
         * Beginner mistake: using MessageDigest.getInstance("MD5") or SHA-256
         * directly. These are fast hashing algorithms — designed for speed —
         * which makes them catastrophically bad for passwords. BCrypt is
         * specifically designed to be slow and to resist GPU acceleration.
         */
        return new BCryptPasswordEncoder(12);
    }
}
