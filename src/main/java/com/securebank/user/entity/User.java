package com.securebank.user.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The User entity is the JPA representation of the 'users' table.
 *
 * Key design decisions explained:
 *
 * UUID primary key: Unlike auto-increment integers (1, 2, 3...),
 * UUIDs cannot be guessed. An attacker cannot iterate /users/1, /users/2
 * to enumerate all users. This is called preventing "insecure direct
 * object reference" (IDOR) — one of the OWASP Top 10 vulnerabilities.
 *
 * @Version for optimistic locking: Before Hibernate saves an update,
 *          it checks that the version column in the DB matches what was read.
 *          If another request updated the row in between, the version won't
 *          match
 *          and Hibernate throws OptimisticLockException instead of silently
 *          overwriting the other request's changes. Critical for concurrent
 *          updates.
 *
 * @PrePersist / @PreUpdate: JPA lifecycle callbacks that automatically
 *             manage createdAt and updatedAt. You never set these manually —
 *             the ORM handles it. This prevents accidental omissions.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "national_id")
    private String nationalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.ROLE_CUSTOMER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verfied = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}
