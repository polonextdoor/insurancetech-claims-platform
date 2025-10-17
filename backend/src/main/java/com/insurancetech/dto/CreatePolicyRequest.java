package com.insurancetech.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new insurance policy
 * Used by admins/agents to create policies for customers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePolicyRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Policy type is required")
    private String policyType;

    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "1000.00", message = "Coverage amount must be at least $1,000")
    @DecimalMax(value = "99999999.99", message = "Coverage amount is too large")
    private BigDecimal coverageAmount;

    @NotNull(message = "Deductible is required")
    @DecimalMin(value = "0.00", message = "Deductible cannot be negative")
    private BigDecimal deductible;

    @NotNull(message = "Premium amount is required")
    @DecimalMin(value = "1.00", message = "Premium must be at least $1")
    private BigDecimal premiumAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
}