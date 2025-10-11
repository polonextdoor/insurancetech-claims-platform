package com.insurancetech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for testing application and database connectivity
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("application", "InsuranceTech Claims Platform");
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseCheck() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("database", "CONNECTED");
            response.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            response.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            response.put("url", connection.getMetaData().getURL());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("database", "DISCONNECTED");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
}
