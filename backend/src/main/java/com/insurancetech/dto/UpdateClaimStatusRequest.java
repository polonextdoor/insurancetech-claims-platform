package com.insurancetech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for updating claim status
 * Used by adjusters/admins to change claim status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClaimStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;

    @Size(max = 1000, message = "Notes must be less than 1000 characters")
    private String notes;

    private BigDecimal approvedAmount;

    private Long assignedAdjusterId;
}