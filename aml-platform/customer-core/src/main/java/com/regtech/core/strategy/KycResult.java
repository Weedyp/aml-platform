package com.regtech.core.strategy;

public record KycResult(
        String status,
        int score,
        String notes
) {}
