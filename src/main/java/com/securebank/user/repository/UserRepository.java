package com.securebank.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.securebank.user.entity.User;

/**
 * Spring Data JPA repository for User entities.
 *
 * Spring generates the full implementation of this interface at runtime —
 * you never write SQL for these operations. The method names follow a
 * naming convention that Spring Data parses:
 *
 * findBy + FieldName → SELECT * FROM users WHERE field_name = ?
 * existsBy + FieldName → SELECT COUNT(*) > 0 FROM users WHERE field_name = ?
 *
 * This is called "derived query methods." They are clean and type-safe,
 * but should only be used for simple queries. For complex joins or
 * aggregations, use @Query with JPQL instead.
 *
 * JpaRepository<User, UUID> gives you these for free:
 * save(), findById(), findAll(), deleteById(), count(), existsById()
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByNationalId(String nationalId);
}
