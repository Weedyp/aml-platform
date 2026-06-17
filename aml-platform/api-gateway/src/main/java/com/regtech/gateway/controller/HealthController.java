package com.regtech.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    /**
     * Endpoint to verify if the API Gateway is online and responsive.
     * Route: GET http://localhost:8080/api/v1/system/health
     */
    @GetMapping("/api/v1/system/health")
    public String checkHealth() {
        // TEMPORARY: Throw this exception to confirm our TSK-1.3 Error Handler intercepts it
//        throw new IllegalStateException("Database pool connections exhausted unexpectedly.");

        // After verifying your error handler works, comment out the line above and uncomment this:
         return "{\"status\": \"UP\", \"message\": \"API Gateway is fully operational.\"}";
    }
}
