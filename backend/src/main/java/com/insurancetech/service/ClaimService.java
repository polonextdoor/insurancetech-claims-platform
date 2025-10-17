package com.insurancetech.service;

import com.insurancetech.dto.ClaimResponse;
import com.insurancetech.dto.CreateClaimRequest;
import com.insurancetech.dto.UpdateClaimStatusRequest;
import com.insurancetech.model.Claim;
import com.insurancetech.model.Policy;
import com.insurancetech.model.User;
import com.insurancetech.repository.ClaimRepository;
import com.insurancetech.repository.PolicyRepository;
import com.insurancetech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling claim operations
 * Manages claim lifecycle, validation, and business rules
 */
@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new claim
     */
    @Transactional
    public ClaimResponse createClaim(CreateClaimRequest request, Long userId) {
        // Validate policy exists and belongs to user
        Policy policy = policyRepository.findById(request.getPolicyId())
            .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (!policy.getUser().getId().equals(userId)) {
            throw new RuntimeException("Policy does not belong to user");
        }

        // Check if policy is active
        if (!policy.getIsActive()) {
            throw new RuntimeException("Policy is not active");
        }

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Create claim
        Claim claim = new Claim();
        claim.setClaimNumber(generateClaimNumber());
        claim.setPolicy(policy);
        claim.setUser(user);
        claim.setIncidentDate(request.getIncidentDate());
        claim.setIncidentDescription(request.getIncidentDescription());
        claim.setIncidentLocation(request.getIncidentLocation());
        claim.setClaimedAmount(request.getClaimedAmount());
        claim.setDeductibleAmount(policy.getDeductible());
        claim.setStatus(Claim.ClaimStatus.SUBMITTED);
        claim.setReportedDate(LocalDateTime.now());
        claim.setSubmittedAt(LocalDateTime.now());

        // Calculate initial risk score
        calculateRiskScore(claim);

        // Save claim
        claim = claimRepository.save(claim);

        return ClaimResponse.fromClaim(claim);
    }

    /**
     * Get claim by ID
     */
    public ClaimResponse getClaimById(Long claimId, Long userId, String userRole) {
        Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Authorization check
        if (!canAccessClaim(claim, userId, userRole)) {
            throw new RuntimeException("Access denied");
        }

        return ClaimResponse.fromClaim(claim);
    }

    /**
     * Get all claims for a user
     */
    public List<ClaimResponse> getClaimsByUser(Long userId) {
        List<Claim> claims = claimRepository.findByUserId(userId);
        return claims.stream()
            .map(ClaimResponse::fromClaim)
            .collect(Collectors.toList());
    }

    /**
     * Get all claims (admin/adjuster only)
     */
    public List<ClaimResponse> getAllClaims() {
        List<Claim> claims = claimRepository.findAll();
        return claims.stream()
            .map(ClaimResponse::fromClaim)
            .collect(Collectors.toList());
    }

    /**
     * Get claims by status
     */
    public List<ClaimResponse> getClaimsByStatus(Claim.ClaimStatus status) {
        List<Claim> claims = claimRepository.findByStatus(status);
        return claims.stream()
            .map(ClaimResponse::fromClaim)
            .collect(Collectors.toList());
    }

    /**
     * Update claim status (adjusters/admins only)
     */
    @Transactional
    public ClaimResponse updateClaimStatus(Long claimId, UpdateClaimStatusRequest request) {
        Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Parse and validate status
        Claim.ClaimStatus newStatus;
        try {
            newStatus = Claim.ClaimStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + request.getStatus());
        }

        // Validate status transition
        validateStatusTransition(claim.getStatus(), newStatus);

        // Update status
        claim.setStatus(newStatus);

        // Update timestamps based on status
        if (newStatus == Claim.ClaimStatus.UNDER_REVIEW && claim.getReviewedAt() == null) {
            claim.setReviewedAt(LocalDateTime.now());
        }
        if (newStatus == Claim.ClaimStatus.CLOSED || newStatus == Claim.ClaimStatus.APPROVED || 
            newStatus == Claim.ClaimStatus.DENIED) {
            claim.setClosedAt(LocalDateTime.now());
        }

        // Update approved amount if provided
        if (request.getApprovedAmount() != null) {
            claim.setApprovedAmount(request.getApprovedAmount());
        }

        // Assign adjuster if provided
        if (request.getAssignedAdjusterId() != null) {
            User adjuster = userRepository.findById(request.getAssignedAdjusterId())
                .orElseThrow(() -> new RuntimeException("Adjuster not found"));
            claim.setAssignedAdjuster(adjuster);
        }

        claim = claimRepository.save(claim);

        return ClaimResponse.fromClaim(claim);
    }

    /**
     * Delete claim (soft delete - change status to CLOSED)
     */
    @Transactional
    public void deleteClaim(Long claimId, Long userId, String userRole) {
        Claim claim = claimRepository.findById(claimId)
            .orElseThrow(() -> new RuntimeException("Claim not found"));

        // Only allow deletion of own claims in DRAFT status, or admin can delete any
        if (!userRole.equals("ADMIN")) {
            if (!claim.getUser().getId().equals(userId)) {
                throw new RuntimeException("Access denied");
            }
            if (claim.getStatus() != Claim.ClaimStatus.DRAFT) {
                throw new RuntimeException("Can only delete draft claims");
            }
        }

        claimRepository.delete(claim);
    }

    // Helper methods

    /**
     * Generate unique claim number
     */
    private String generateClaimNumber() {
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String claimNumber = "CLM-" + uuid;
        
        // Ensure uniqueness
        while (claimRepository.existsByClaimNumber(claimNumber)) {
            uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            claimNumber = "CLM-" + uuid;
        }
        
        return claimNumber;
    }

    /**
     * Calculate risk score based on claim attributes
     */
    private void calculateRiskScore(Claim claim) {
        int score = 0;

        // High claimed amount increases risk
        if (claim.getClaimedAmount().compareTo(claim.getPolicy().getCoverageAmount().multiply(
            java.math.BigDecimal.valueOf(0.5))) > 0) {
            score += 30;
        }

        // Set risk level based on score
        if (score >= 70) {
            claim.setRiskLevel(Claim.RiskLevel.CRITICAL);
        } else if (score >= 50) {
            claim.setRiskLevel(Claim.RiskLevel.HIGH);
        } else if (score >= 30) {
            claim.setRiskLevel(Claim.RiskLevel.MEDIUM);
        } else {
            claim.setRiskLevel(Claim.RiskLevel.LOW);
        }

        claim.setRiskScore(score);
    }

    /**
     * Check if user can access claim
     */
    private boolean canAccessClaim(Claim claim, Long userId, String userRole) {
        // Admins and adjusters can access all claims
        if (userRole.equals("ADMIN") || userRole.equals("ADJUSTER")) {
            return true;
        }

        // Customers can only access their own claims
        return claim.getUser().getId().equals(userId);
    }

    /**
     * Validate status transition
     */
    private void validateStatusTransition(Claim.ClaimStatus currentStatus, Claim.ClaimStatus newStatus) {
        // Define valid transitions
        // For now, we'll allow any transition (can add stricter rules later)
        // Example: Can't go from CLOSED back to SUBMITTED
        if (currentStatus == Claim.ClaimStatus.CLOSED || currentStatus == Claim.ClaimStatus.DENIED) {
            if (newStatus != Claim.ClaimStatus.CLOSED && newStatus != Claim.ClaimStatus.DENIED) {
                throw new RuntimeException("Cannot reopen closed or denied claims");
            }
        }
    }
}