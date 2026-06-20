package com.securebank.user.entity;

/**
 * Defines the roles a user can have in the system.
 *
 * We use an enum instead of a plain String for two reasons:
 * 1. The compiler enforces valid values — you cannot accidentally assign
 * role = "ADMINN" (typo) because it won't compile.
 * 2. The @Enumerated(EnumType.STRING) annotation on the entity stores
 * "CUSTOMER" in the database, not 0 or 1. This makes the database
 * human-readable and prevents data corruption if the enum order changes.
 *
 * The "ROLE_" prefix is a Spring Security convention. When you call
 * hasRole("CUSTOMER"), Spring internally checks for "ROLE_CUSTOMER".
 * By storing the prefix in the enum name, the mapping is automatic.
 */
public enum UserRole {
    ROLE_CUSTOMER,
    ROLE_ADMIN
}
