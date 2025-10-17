package com.insurancetech.controller;

import com.insurancetech.dto.CreatePolicyRequest;
import com.insurancetech.dto.PolicyResponse;
import com.insurancetech.security.JwtUtil;
import com.insurancetech.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for policy management endpoints
 * Handles policy CRUD operations with role-based access control
 */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Create a new policy (admin/agent only)
     * POST /api/policies
     */
    @PostMapping
    public ResponseEntity<?> createPolicy(
            @Valid @RequestBody CreatePolicyRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization - only admins and agents can create policies
            if (!userRole.equals("ADMIN") && !userRole.equals("AGENT")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins and agents only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            PolicyResponse response = policyService.createPolicy(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get policy by ID
     * GET /api/policies/{id}
     * Authorization: Owner or admin/agent
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPolicyById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);
            String userRole = jwtUtil.extractRole(token);

            PolicyResponse response = policyService.getPolicyById(id, userId, userRole);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
    }

    /**
     * Get all policies for current user
     * GET /api/policies/my-policies
     */
    @GetMapping("/my-policies")
    public ResponseEntity<?> getMyPolicies(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            List<PolicyResponse> policies = policyService.getPoliciesByUser(userId);
            return ResponseEntity.ok(policies);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all active policies for current user
     * GET /api/policies/my-policies/active
     */
    @GetMapping("/my-policies/active")
    public ResponseEntity<?> getMyActivePolicies(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.extractUserId(token);

            List<PolicyResponse> policies = policyService.getActivePoliciesByUser(userId);
            return ResponseEntity.ok(policies);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all policies (admin/agent only)
     * GET /api/policies
     */
    @GetMapping
    public ResponseEntity<?> getAllPolicies(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization
            if (!userRole.equals("ADMIN") && !userRole.equals("AGENT")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins and agents only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            List<PolicyResponse> policies = policyService.getAllPolicies();
            return ResponseEntity.ok(policies);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Deactivate policy (admin only)
     * PUT /api/policies/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivatePolicy(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization
            if (!userRole.equals("ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            PolicyResponse response = policyService.deactivatePolicy(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete policy (admin only)
     * DELETE /api/policies/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolicy(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String userRole = jwtUtil.extractRole(token);

            // Check authorization
            if (!userRole.equals("ADMIN")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admins only.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            policyService.deletePolicy(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Policy deleted successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}