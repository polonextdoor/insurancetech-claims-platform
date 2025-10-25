package com.insurancetech.service;

import com.insurancetech.dto.CreateClaimRequest;
import com.insurancetech.dto.ml.MLClaimRequest;
import com.insurancetech.dto.ml.MLRiskAssessment;
import com.insurancetech.model.Claim;
import com.insurancetech.model.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

@Service
public class MLRiskAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(MLRiskAssessmentService.class);
    
    private final WebClient webClient;
    private final boolean mlServiceEnabled;
    private final int timeoutSeconds;

    public MLRiskAssessmentService(
            @Value("${app.ml-service.url}") String mlServiceUrl,
            @Value("${app.ml-service.enabled}") boolean mlServiceEnabled,
            @Value("${app.ml-service.timeout-seconds}") int timeoutSeconds) {
        
        this.mlServiceEnabled = mlServiceEnabled;
        this.timeoutSeconds = timeoutSeconds;
        
        this.webClient = WebClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
        
        logger.info("ML Service initialized: {} (enabled: {})", mlServiceUrl, mlServiceEnabled);
    }

    /**
     * Assess claim risk using ML service
     * Called BEFORE claim is saved to database
     * 
     * @param request The claim creation request
     * @param policy The associated policy
     * @param customerClaimsCount Number of previous claims by this customer
     * @return ML assessment or null if service unavailable
     */
    public MLRiskAssessment assessClaimRisk(CreateClaimRequest request, Policy policy, 
                                            int customerClaimsCount) {
        
        if (!mlServiceEnabled) {
            logger.info("ML service disabled, will use fallback risk calculation");
            return null;
        }
        
        try {
            // Build request using CreateClaimRequest (no claim ID needed)
            MLClaimRequest mlRequest = buildMLRequest(request, policy, customerClaimsCount);
            
            logger.info("Calling ML service for policy {} (claim not yet saved)", 
                    policy.getPolicyNumber());
            
            // Make synchronous call to ML service
            MLRiskAssessment assessment = webClient
                    .post()
                    .uri("/api/ml/assess-risk")
                    .bodyValue(mlRequest)
                    .retrieve()
                    .bodyToMono(MLRiskAssessment.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            
            logger.info("ML assessment complete: {} (score: {})", 
                    assessment.getRiskLevel(), assessment.getRiskScore());
            
            return assessment;
            
        } catch (WebClientResponseException e) {
            logger.error("ML service responded with error: {} - {}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Failed to call ML service: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Build ML request from claim request and policy data
     * Note: Using claim_id=0 as temporary ID since claim not yet saved
     */
    private MLClaimRequest buildMLRequest(CreateClaimRequest request, Policy policy, 
                                         int customerClaimsCount) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        
        return MLClaimRequest.builder()
                .claimId(0)  // Temporary ID - ML service doesn't actually use this
                .policyType(policy.getPolicyType().name())
                .claimedAmount(request.getClaimedAmount().doubleValue())
                .coverageAmount(policy.getCoverageAmount().doubleValue())
                .deductible(policy.getDeductible().doubleValue())
                .incidentDescription(request.getIncidentDescription())
                .incidentDate(request.getIncidentDate().format(formatter))
                .policyStartDate(policy.getStartDate().format(formatter))
                .customerClaimsCount(customerClaimsCount)
                .build();
    }

    /**
     * Fallback risk calculation (used when ML service unavailable)
     * Simple rule-based assessment
     */
    public void calculateFallbackRisk(Claim claim, Policy policy) {
        int score = 0;

        // High claimed amount increases risk
        BigDecimal halfCoverage = policy.getCoverageAmount().multiply(BigDecimal.valueOf(0.5));
        if (claim.getClaimedAmount().compareTo(halfCoverage) > 0) {
            score += 40;
        }
        
        // Very high amount is critical
        BigDecimal threeQuarterCoverage = policy.getCoverageAmount().multiply(BigDecimal.valueOf(0.75));
        if (claim.getClaimedAmount().compareTo(threeQuarterCoverage) > 0) {
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
        claim.setFraudScore(BigDecimal.valueOf(score / 100.0));
        claim.setFraudFlag(score >= 70);
        
        logger.info("Fallback risk calculation: {} (score: {})", 
                claim.getRiskLevel(), score);
    }
}