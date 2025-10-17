package com.insurancetech.repository;

import com.insurancetech.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Claim entity
 * Provides database operations for claims table
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    /**
     * Find claim by claim number
     */
    Optional<Claim> findByClaimNumber(String claimNumber);

    /**
     * Check if claim number exists
     */
    boolean existsByClaimNumber(String claimNumber);

    /**
     * Find all claims for a specific user
     */
    List<Claim> findByUserId(Long userId);

    /**
     * Find all claims by status
     */
    List<Claim> findByStatus(Claim.ClaimStatus status);

    /**
     * Find claims assigned to a specific adjuster
     */
    List<Claim> findByAssignedAdjusterId(Long adjusterId);

    /**
     * Find all claims for a specific policy
     */
    List<Claim> findByPolicyId(Long policyId);

    /**
     * Find claims with fraud flag set
     */
    List<Claim> findByFraudFlagTrue();

    /**
     * Find claims by user and status
     */
    List<Claim> findByUserIdAndStatus(Long userId, Claim.ClaimStatus status);

    /**
     * Count claims by status
     */
    long countByStatus(Claim.ClaimStatus status);

    /**
     * Count claims for a user
     */
    long countByUserId(Long userId);

    /**
     * Find recent claims (custom query)
     */
    @Query("SELECT c FROM Claim c WHERE c.submittedAt IS NOT NULL ORDER BY c.submittedAt DESC")
    List<Claim> findRecentClaims();
}