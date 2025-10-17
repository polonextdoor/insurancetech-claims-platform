package com.insurancetech.dto;

import com.insurancetech.model.Policy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for policy information in API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponse {

    private Long id;
    private String policyNumber;
    
    private Long userId;
    private String customerName;
    
    private String policyType;
    private BigDecimal coverageAmount;
    private BigDecimal deductible;
    private BigDecimal premiumAmount;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Policy entity to PolicyResponse DTO
     */
    public static PolicyResponse fromPolicy(Policy policy) {
        PolicyResponse response = new PolicyResponse();
        
        response.setId(policy.getId());
        response.setPolicyNumber(policy.getPolicyNumber());
        
        if (policy.getUser() != null) {
            response.setUserId(policy.getUser().getId());
            response.setCustomerName(
                policy.getUser().getFirstName() + " " + policy.getUser().getLastName()
            );
        }
        
        response.setPolicyType(policy.getPolicyType().name());
        response.setCoverageAmount(policy.getCoverageAmount());
        response.setDeductible(policy.getDeductible());
        response.setPremiumAmount(policy.getPremiumAmount());
        response.setStartDate(policy.getStartDate());
        response.setEndDate(policy.getEndDate());
        response.setIsActive(policy.getIsActive());
        response.setCreatedAt(policy.getCreatedAt());
        response.setUpdatedAt(policy.getUpdatedAt());
        
        return response;
    }
}