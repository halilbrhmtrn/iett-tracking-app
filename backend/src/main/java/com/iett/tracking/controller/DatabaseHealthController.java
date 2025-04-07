package com.iett.tracking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/db-health")
public class DatabaseHealthController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseHealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> checkDatabaseConnection() {
        Map<String, String> response = new HashMap<>();
        
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                response.put("status", "UP");
                response.put("message", "Database connection is successful");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("message", "Database connection check failed");
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Database connection failed: " + e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }
} 