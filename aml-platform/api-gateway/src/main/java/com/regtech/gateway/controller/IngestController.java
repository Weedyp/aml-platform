package com.regtech.gateway.controller;


import com.regtech.gateway.dto.ApiResponse;
import com.regtech.gateway.dto.IngestResponseDto;
import com.regtech.gateway.service.IngestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestController {

    private final IngestService ingestService;

    public IngestController(IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IngestResponseDto>> uploadBatch(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt) {

        // 1. Extract tenant identity securely from the validated token
        String tenantId = jwt.getClaimAsString("tenant_id");

        // Fallback for local testing if you generated a basic token without custom claims
        if (tenantId == null) {
            tenantId = jwt.getSubject();
        }

        // 2. Pass to the service layer
        IngestResponseDto result = ingestService.stageFile(file, tenantId);

        // 3. Return our standardized corporate wrapper
        return ResponseEntity.ok(ApiResponse.success(result, "File processed by staging zone."));
    }
}
