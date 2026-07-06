package com.regtech.core.dto;

public record CoreBatchReadyDto(
        String tenantId,
        int totalRowsInserted,
        long timestamp
) {}
