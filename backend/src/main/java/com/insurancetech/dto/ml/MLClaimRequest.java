package com.insurancetech.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending claim data to ML service
 * Uses snake_case naming convention to match Python FastAPI expectations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLClaimRequest {
    
    @JsonProperty("claim_id")
    private Integer claimId;
    
    @JsonProperty("policy_type")
    private String policyType;
    
    @JsonProperty("claimed_amount")
    private Double claimedAmount;
    
    @JsonProperty("coverage_amount")
    private Double coverageAmount;
    
    @JsonProperty("deductible")
    private Double deductible;
    
    @JsonProperty("incident_description")
    private String incidentDescription;
    
    @JsonProperty("incident_date")
    private String incidentDate;
    
    @JsonProperty("policy_start_date")
    private String policyStartDate;
    
    @JsonProperty("customer_claims_count")
    private Integer customerClaimsCount;
}