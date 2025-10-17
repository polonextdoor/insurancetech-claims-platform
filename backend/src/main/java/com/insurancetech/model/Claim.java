package com.insurancetech.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Claim entity representing insurance claims
 * Maps to claims table in database
 */
@Entity
@Table(name = "claims")
@EntityListeners(AuditingEntityListener.class)
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", unique = true, nullable = false, length = 50)
    private String claimNumber;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    @JsonIgnore
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_adjuster_id")
    @JsonIgnore
    private User assignedAdjuster;

    // Claim details
    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;

    @Column(name = "reported_date")
    private LocalDateTime reportedDate;

    @Column(name = "incident_description", nullable = false, columnDefinition = "TEXT")
    private String incidentDescription;

    @Column(name = "incident_location", length = 255)
    private String incidentLocation;

    // Financial details
    @Column(name = "claimed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal claimedAmount;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount;

    @Column(name = "deductible_amount", precision = 10, scale = 2)
    private BigDecimal deductibleAmount;

    // Status and workflow
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClaimStatus status = ClaimStatus.DRAFT;

    // Risk assessment
    @Column(name = "risk_score")
    private Integer riskScore = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;

    // Fraud detection
    @Column(name = "fraud_flag")
    private Boolean fraudFlag = false;

    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore = BigDecimal.ZERO;

    // Timestamps
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Claim() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAssignedAdjuster() {
        return assignedAdjuster;
    }

    public void setAssignedAdjuster(User assignedAdjuster) {
        this.assignedAdjuster = assignedAdjuster;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public LocalDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }

    public String getIncidentDescription() {
        return incidentDescription;
    }

    public void setIncidentDescription(String incidentDescription) {
        this.incidentDescription = incidentDescription;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(String incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public BigDecimal getClaimedAmount() {
        return claimedAmount;
    }

    public void setClaimedAmount(BigDecimal claimedAmount) {
        this.claimedAmount = claimedAmount;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getDeductibleAmount() {
        return deductibleAmount;
    }

    public void setDeductibleAmount(BigDecimal deductibleAmount) {
        this.deductibleAmount = deductibleAmount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Boolean getFraudFlag() {
        return fraudFlag;
    }

    public void setFraudFlag(Boolean fraudFlag) {
        this.fraudFlag = fraudFlag;
    }

    public BigDecimal getFraudScore() {
        return fraudScore;
    }

    public void setFraudScore(BigDecimal fraudScore) {
        this.fraudScore = fraudScore;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Claim status enum matching database
     */
    public enum ClaimStatus {
        DRAFT,
        SUBMITTED,
        UNDER_REVIEW,
        INVESTIGATING,
        APPROVED,
        DENIED,
        CLOSED
    }

    /**
     * Risk level enum matching database
     */
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}