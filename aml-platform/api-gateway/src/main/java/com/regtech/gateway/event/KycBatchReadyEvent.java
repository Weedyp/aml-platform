package com.regtech.gateway.event;

public record KycBatchReadyEvent (
        String tenantId,
        int totalRowsInserted,
        long timestamp
){}
