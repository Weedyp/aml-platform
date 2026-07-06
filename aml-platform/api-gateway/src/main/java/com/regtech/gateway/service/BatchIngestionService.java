package com.regtech.gateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.regtech.gateway.event.KycBatchReadyEvent;
import com.regtech.gateway.event.KycEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchIngestionService {

    private static final Logger log = LoggerFactory.getLogger(BatchIngestionService.class);
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final KycEventPublisher eventPublisher;

    public BatchIngestionService(NamedParameterJdbcTemplate jdbcTemplate,KycEventPublisher eventPublisher) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventPublisher=eventPublisher;
    }

    public void insertBatch(String tenantId,List<JsonNode> validRows) {
        if (validRows.isEmpty()) return;

        // Native SQL for maximum speed.
        // We insert into a generic staging table that accepts raw strings.
        String sql = """
            INSERT INTO stg_customer_upload (customer_id, country_code, risk_score) 
            VALUES (:customerId, :countryCode, :riskScore)
            """;

        // Convert the JSON nodes into SQL parameters
        MapSqlParameterSource[] batchParams = validRows.stream()
                .map(node -> new MapSqlParameterSource()
                        .addValue("customerId", node.path("customerId").asText(null))
                        .addValue("countryCode", node.path("countryCode").asText(null))
                        .addValue("riskScore", node.path("riskScore").asInt(0)))
                .toArray(MapSqlParameterSource[]::new);

        // Execute the massive batch insert
        int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchParams);
        log.info("Successfully materialized {} rows into SQL Server staging table.", updateCounts.length);

        KycBatchReadyEvent event =new KycBatchReadyEvent(tenantId,updateCounts.length,System.currentTimeMillis());
        eventPublisher.publishBatchReady(event);
    }
}
