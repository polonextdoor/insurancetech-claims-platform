package com.insurancetech.controller;

import com.insurancetech.dto.ClaimResponse;
import com.insurancetech.dto.CreateClaimRequest;
import com.insurancetech.dto.UpdateClaimStatusRequest;
import com.insurancetech.model.Claim;
import com.insurancetech.security.JwtUtil;
import com.insurancetech.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for claim management endpoints
 * Handles claim CRUD operations with role-based access control
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create a new claim
     * POST /api/claims
     * Requires: CUSTOMER, AGENT role
     */
    @PostMapping
    public ResponseEntity<?> createClaim(
            @Valid @RequestBody CreateClaimRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Extract user info from JWT token
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            ClaimResponse response = claimService.createClaim(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get claim by ID
     * GET /api/claims/{id}
     * Authorization: Owner, adjusters, or admins only
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClaimById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String userRole = jwtUtil.extractRole(token);

            ClaimResponse response = claimService.getClaimById(id, userId, userRole);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    /**
     * Get all claims for current user
     * GET /api/claims/my-claims
     * Requires: Any authenticated user
     */
    @GetMapping("/my-claims")
    public ResponseEntity<?> getMyClai(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            List<ClaimResponse> claims = claimService.getClaimsByUser(userId);
            return ResponseEntity.ok(claims);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all claims (admin/adjuster only)
     * GET /api/claims
     * Requires: ADMIN or ADJUSTER role
     */
    @GetMapping
    public ResponseEntity<?> getAllClaims(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization
            if (!userRole.equals("ADMIN") && !userRole.equals("ADJUSTER")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins and adjusters only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<ClaimResponse> claims = claimService.getAllClaims();
            return ResponseEntity.ok(claims);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get claims by status (admin/adjuster only)
     * GET /api/claims/status/{status}
     * Requires: ADMIN or ADJUSTER role
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getClaimsByStatus(
            @PathVariable String status,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization
            if (!userRole.equals("ADMIN") && !userRole.equals("ADJUSTER")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins and adjusters only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            Claim.ClaimStatus claimStatus = Claim.ClaimStatus.valueOf(status.toUpperCase());
            List<ClaimResponse> claims = claimService.getClaimsByStatus(claimStatus);
            return ResponseEntity.ok(claims);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid status: " + status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update claim status (adjuster/admin only)
     * PUT /api/claims/{id}/status
     * Requires: ADMIN or ADJUSTER role
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateClaimStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClaimStatusRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization
            if (!userRole.equals("ADMIN") && !userRole.equals("ADJUSTER")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins and adjusters only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            ClaimResponse response = claimService.updateClaimStatus(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete claim
     * DELETE /api/claims/{id}
     * Authorization: Owner (draft only) or admin
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClaim(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String userRole = jwtUtil.extractRole(token);

            claimService.deleteClaim(id, userId, userRole);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Claim deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}