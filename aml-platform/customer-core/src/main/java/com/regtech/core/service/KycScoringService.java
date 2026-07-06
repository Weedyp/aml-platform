package com.regtech.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class KycScoringService {

    private static final Logger log = LoggerFactory.getLogger(KycScoringService.class);
    private final JdbcTemplate jdbcTemplate;

    public KycScoringService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void processPendingBatch(String tenantId) {
        log.info("🔍 Initiating AML Background Checks for tenant: {}", tenantId);

        // 1. Fetch all pending records
        String selectSql = "SELECT id, customer_id, country_code, risk_score FROM stg_customer_upload WHERE kyc_status = 'PENDING'";
        List<Map<String, Object>> pendingRecords = jdbcTemplate.queryForList(selectSql);

        if (pendingRecords.isEmpty()) {
            log.info("No pending records found for KYC processing.");
            return;
        }

        // 2. Process each record through the Mock AML Rules Engine
        for (Map<String, Object> record : pendingRecords) {
            long dbId = ((Number) record.get("id")).longValue();
            String customerId = (String) record.get("customer_id");
            String country = (String) record.get("country_code");

            String newStatus;
            String notes;
            int currentScore = record.get("risk_score") != null ? ((Number) record.get("risk_score")).intValue() : 0;
            int finalScore;

            // --- THE MOCK ALGORITHM ---
            if (List.of("KP", "IR", "SY").contains(country)) {
                newStatus = "BLOCKED";
                finalScore = 100;
                notes = "FATF Blacklist Match: High-Risk Jurisdiction";
            } else if (List.of("RU", "BY").contains(country)) {
                newStatus = "MANUAL_REVIEW";
                finalScore = 85;
                notes = "Sanctions Watchlist Match";
            } else if (currentScore > 75) {
                newStatus = "MANUAL_REVIEW";
                finalScore = currentScore;
                notes = "High Initial Risk Score provided by bank";
            } else {
                newStatus = "APPROVED";
                finalScore = currentScore;
                notes = "Passed automated screening";
            }

            // 3. Update the database with the verdict
            String updateSql = "UPDATE stg_customer_upload SET kyc_status = ?, risk_score = ?, kyc_notes = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, newStatus, finalScore, notes, dbId);

            log.info("Customer {} processed. Status: {}, Score: {}", customerId, newStatus, finalScore);
        }

        log.info("✅ KYC Batch Processing Complete. {} records screened.", pendingRecords.size());
    }
}
