package com.insurancetech.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating a new claim
 * Used when customers submit insurance claims
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequest {

    @NotNull(message = "Policy ID is required")
    private Long policyId;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    private LocalDate incidentDate;

    @NotBlank(message = "Incident description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String incidentDescription;

    @Size(max = 255, message = "Location must be less than 255 characters")
    private String incidentLocation;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(value = "0.01", message = "Claimed amount must be greater than 0")
    @DecimalMax(value = "9999999999.99", message = "Claimed amount is too large")
    private BigDecimal claimedAmount;
}