package com.regtech.gateway.dto;

public record IngestResponseDto(
        String trackingId,
        String checksum,
        String status,
        long sizeBytes
) {}
