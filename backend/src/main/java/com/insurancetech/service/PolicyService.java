package com.insurancetech.service;

import com.insurancetech.dto.CreatePolicyRequest;
import com.insurancetech.dto.PolicyResponse;
import com.insurancetech.model.Policy;
import com.insurancetech.model.User;
import com.insurancetech.repository.PolicyRepository;
import com.insurancetech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling policy operations
 * Manages policy lifecycle and business rules
 */
@Service
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new policy (admin/agent only)
     */
    @Transactional
    public PolicyResponse createPolicy(CreatePolicyRequest request) {
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("End date must be after start date");
        }

        // Parse policy type
        Policy.PolicyType policyType;
        try {
            policyType = Policy.PolicyType.valueOf(request.getPolicyType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid policy type: " + request.getPolicyType());
        }

        // Create policy
        Policy policy = new Policy();
        policy.setPolicyNumber(generatePolicyNumber(policyType));
        policy.setUser(user);
        policy.setPolicyType(policyType);
        policy.setCoverageAmount(request.getCoverageAmount());
        policy.setDeductible(request.getDeductible());
        policy.setPremiumAmount(request.getPremiumAmount());
        policy.setStartDate(request.getStartDate());
        policy.setEndDate(request.getEndDate());
        policy.setIsActive(true);

        policy = policyRepository.save(policy);

        return PolicyResponse.fromPolicy(policy);
    }

    /**
     * Get policy by ID
     */
    public PolicyResponse getPolicyById(Long policyId, Long userId, String userRole) {
        Policy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found"));

        // Authorization check
        if (!canAccessPolicy(policy, userId, userRole)) {
            throw new RuntimeException("Access denied");
        }

        return PolicyResponse.fromPolicy(policy);
    }

    /**
     * Get all policies for a user
     */
    public List<PolicyResponse> getPoliciesByUser(Long userId) {
        List<Policy> policies = policyRepository.findByUserId(userId);
        return policies.stream()
            .map(PolicyResponse::fromPolicy)
            .collect(Collectors.toList());
    }

    /**
     * Get all active policies for a user
     */
    public List<PolicyResponse> getActivePoliciesByUser(Long userId) {
        List<Policy> policies = policyRepository.findByUserIdAndIsActiveTrue(userId);
        return policies.stream()
            .map(PolicyResponse::fromPolicy)
            .collect(Collectors.toList());
    }

    /**
     * Get all policies (admin only)
     */
    public List<PolicyResponse> getAllPolicies() {
        List<Policy> policies = policyRepository.findAll();
        return policies.stream()
            .map(PolicyResponse::fromPolicy)
            .collect(Collectors.toList());
    }

    /**
     * Deactivate policy (admin only)
     */
    @Transactional
    public PolicyResponse deactivatePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found"));

        policy.setIsActive(false);
        policy = policyRepository.save(policy);

        return PolicyResponse.fromPolicy(policy);
    }

    /**
     * Delete policy (admin only)
     */
    @Transactional
    public void deletePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
            .orElseThrow(() -> new RuntimeException("Policy not found"));

        // Check if policy has claims
        // For now, we'll allow deletion (in production, you might prevent this)
        policyRepository.delete(policy);
    }

    // Helper methods

    /**
     * Generate unique policy number
     */
    private String generatePolicyNumber(Policy.PolicyType policyType) {
        String prefix = "POL-" + policyType.name() + "-";
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String policyNumber = prefix + uuid;

        // Ensure uniqueness
        while (policyRepository.findByPolicyNumber(policyNumber).isPresent()) {
            uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            policyNumber = prefix + uuid;
        }

        return policyNumber;
    }

    /**
     * Check if user can access policy
     */
    private boolean canAccessPolicy(Policy policy, Long userId, String userRole) {
        // Admins and agents can access all policies
        if (userRole.equals("ADMIN") || userRole.equals("AGENT")) {
            return true;
        }

        // Customers can only access their own policies
        return policy.getUser().getId().equals(userId);
    }
}