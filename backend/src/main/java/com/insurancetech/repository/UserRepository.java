package com.insurancetech.repository;

import com.insurancetech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Provides database operations for users table
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     * Used for login and checking if email exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists
     * Used during registration
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email and active status
     * Only returns active users
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(Long userId, LocalDateTime loginTime);
}
