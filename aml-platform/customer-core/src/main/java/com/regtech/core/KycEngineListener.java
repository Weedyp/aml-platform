package com.regtech.core;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regtech.core.dto.CoreBatchReadyDto;
import com.regtech.core.event.IntegrationEvent;
import com.regtech.core.service.KycScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class KycEngineListener {

    private static final Logger log = LoggerFactory.getLogger(KycEngineListener.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final KycScoringService kycScoringService;

    // Inject the new service
    public KycEngineListener(KycScoringService kycScoringService) {
        this.kycScoringService = kycScoringService;
    }
    @Async
    @EventListener
    public void onIntegrationEvent(IntegrationEvent event) {
        // Mimicking a Kafka Consumer filtering by Topic
        if (!"KYC_BATCH_READY".equals(event.topic())) {
            return;
        }

        try {
            // Deserialize the raw JSON back into the Core's local DTO
            CoreBatchReadyDto batchData = objectMapper.readValue(event.jsonPayload(), CoreBatchReadyDto.class);

            log.info("🎯 KYC ENGINE TRIGGERED: Acknowledged batch for {}. Preparing to screen {} rows.",
                    batchData.tenantId(), batchData.totalRowsInserted());

            kycScoringService.processPendingBatch(batchData.tenantId());
        } catch (Exception e) {
            log.error("Failed to deserialize KYC Integration Event", e);
        }
    }
}