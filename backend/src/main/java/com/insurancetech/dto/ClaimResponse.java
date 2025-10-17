package com.insurancetech.dto;

import com.insurancetech.model.Claim;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for claim information in API responses
 * Provides formatted claim data to clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimResponse {

    private Long id;
    private String claimNumber;
    
    // Basic info
    private Long policyId;
    private String policyNumber;
    private String policyType;
    
    private Long userId;
    private String customerName;
    
    private Long assignedAdjusterId;
    private String assignedAdjusterName;
    
    // Claim details
    private LocalDate incidentDate;
    private LocalDateTime reportedDate;
    private String incidentDescription;
    private String incidentLocation;
    
    // Financial
    private BigDecimal claimedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal deductibleAmount;
    
    // Status
    private String status;
    private String riskLevel;
    private Integer riskScore;
    private Boolean fraudFlag;
    private BigDecimal fraudScore;
    
    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Claim entity to ClaimResponse DTO
     */
    public static ClaimResponse fromClaim(Claim claim) {
        ClaimResponse response = new ClaimResponse();
        
        response.setId(claim.getId());
        response.setClaimNumber(claim.getClaimNumber());
        
        // Policy info
        if (claim.getPolicy() != null) {
            response.setPolicyId(claim.getPolicy().getId());
            response.setPolicyNumber(claim.getPolicy().getPolicyNumber());
            response.setPolicyType(claim.getPolicy().getPolicyType().name());
        }
        
        // User info
        if (claim.getUser() != null) {
            response.setUserId(claim.getUser().getId());
            response.setCustomerName(
                claim.getUser().getFirstName() + " " + claim.getUser().getLastName()
            );
        }
        
        // Adjuster info
        if (claim.getAssignedAdjuster() != null) {
            response.setAssignedAdjusterId(claim.getAssignedAdjuster().getId());
            response.setAssignedAdjusterName(
                claim.getAssignedAdjuster().getFirstName() + " " + 
                claim.getAssignedAdjuster().getLastName()
            );
        }
        
        // Claim details
        response.setIncidentDate(claim.getIncidentDate());
        response.setReportedDate(claim.getReportedDate());
        response.setIncidentDescription(claim.getIncidentDescription());
        response.setIncidentLocation(claim.getIncidentLocation());
        
        // Financial
        response.setClaimedAmount(claim.getClaimedAmount());
        response.setApprovedAmount(claim.getApprovedAmount());
        response.setDeductibleAmount(claim.getDeductibleAmount());
        
        // Status
        response.setStatus(claim.getStatus().name());
        response.setRiskLevel(claim.getRiskLevel().name());
        response.setRiskScore(claim.getRiskScore());
        response.setFraudFlag(claim.getFraudFlag());
        response.setFraudScore(claim.getFraudScore());
        
        // Timestamps
        response.setSubmittedAt(claim.getSubmittedAt());
        response.setReviewedAt(claim.getReviewedAt());
        response.setClosedAt(claim.getClosedAt());
        response.setCreatedAt(claim.getCreatedAt());
        response.setUpdatedAt(claim.getUpdatedAt());
        
        return response;
    }
}