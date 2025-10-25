package com.insurancetech.dto.ml;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for receiving risk assessment results from ML service
 * Uses snake_case naming convention to match Python FastAPI response format
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLRiskAssessment {
    
    @JsonProperty("claim_id")
    private Integer claimId;
    
    @JsonProperty("risk_score")
    private Integer riskScore;
    
    @JsonProperty("risk_level")
    private String riskLevel;
    
    @JsonProperty("fraud_probability")
    private Double fraudProbability;
    
    @JsonProperty("explanation")
    private Map<String, Object> explanation;
    
    @JsonProperty("llm_analysis")
    private String llmAnalysis;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
}