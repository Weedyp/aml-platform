package com.regtech.core.service;

import com.regtech.core.strategy.KycResult;
import com.regtech.core.strategy.KycRuleStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class KycScoringService {

    private static final Logger log = LoggerFactory.getLogger(KycScoringService.class);
    private final JdbcTemplate jdbcTemplate;

    // Spring automatically populates this list with your 3 rules
    private final List<KycRuleStrategy> amlRules;

    public KycScoringService(JdbcTemplate jdbcTemplate, List<KycRuleStrategy> amlRules) {
        this.jdbcTemplate = jdbcTemplate;
        this.amlRules = amlRules;
    }

    public void processPendingBatch(String tenantId) {
        log.info("🔍 Initiating AML Background Checks for tenant: {}", tenantId);

        String selectSql = "SELECT id, customer_id, country_code, risk_score FROM stg_customer_upload WHERE kyc_status = 'PENDING'";
        List<Map<String, Object>> pendingRecords = jdbcTemplate.queryForList(selectSql);

        if (pendingRecords.isEmpty()) {
            return;
        }

        for (Map<String, Object> record : pendingRecords) {
            long dbId = ((Number) record.get("id")).longValue();
            String customerId = (String) record.get("customer_id");
            String country = (String) record.get("country_code");
            int currentScore = record.get("risk_score") != null ? ((Number) record.get("risk_score")).intValue() : 0;

            // Default fallback values
            String newStatus = "APPROVED";
            int finalScore = currentScore;
            String notes = "Passed automated screening";

            // --- THE STRATEGY ENGINE ---
            for (KycRuleStrategy rule : amlRules) {
                Optional<KycResult> result = rule.evaluate(country, currentScore);

                if (result.isPresent()) {
                    newStatus = result.get().status();
                    finalScore = result.get().score();
                    notes = result.get().notes();
                    break; // Stop evaluating rules once a hit is found
                }
            }

            String updateSql = "UPDATE stg_customer_upload SET kyc_status = ?, risk_score = ?, kyc_notes = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, newStatus, finalScore, notes, dbId);

            log.info("Customer {} processed. Status: {}, Score: {}", customerId, newStatus, finalScore);
        }

        log.info("✅ KYC Batch Processing Complete. {} records screened.", pendingRecords.size());
    }
}