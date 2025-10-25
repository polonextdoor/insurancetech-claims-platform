package com.insurancetech.controller;

import com.insurancetech.service.MLRiskAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/ml")
public class MLTestController {

    @Autowired
    private MLRiskAssessmentService mlRiskAssessmentService;

    @GetMapping("/status")
    public ResponseEntity<?> checkMLService() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "ML service integration test");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}