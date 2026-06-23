package com.regtech.gateway.controller;

import com.regtech.gateway.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    /**
     * Endpoint to verify if the API Gateway is online and responsive.
     * Route: GET http://localhost:8080/api/v1/system/health
     */
    @GetMapping("/api/v1/system/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkHealth() {

        // We can pass any object as data. Here, a simple Map works perfectly.
        Map<String, String> statusData = Map.of("status", "UP");

        return ResponseEntity.ok(
                ApiResponse.success(statusData, "API Gateway is fully operational.")
        );
    }
}
