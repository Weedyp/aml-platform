package com.regtech.gateway.event;

import tools.jackson.databind.ObjectMapper;
import com.regtech.core.event.IntegrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringKycEventPublisher implements KycEventPublisher{
    private static final Logger log = LoggerFactory.getLogger(SpringKycEventPublisher.class);
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper;

    public SpringKycEventPublisher(ApplicationEventPublisher applicationEventPublisher, ObjectMapper objectMapper){
        this.applicationEventPublisher=applicationEventPublisher;
        this.objectMapper=objectMapper;
    }

    @Override
    public void publishBatchReady(KycBatchReadyEvent event) {
        try {
            // Serialize the local Gateway object into a raw JSON string
            String jsonPayload = objectMapper.writeValueAsString(event);

            log.info("📢 BROADCASTING JSON PAYLOAD: Topic: KYC_BATCH_READY, Payload: {}", jsonPayload);

            // Publish the generic Integration Event
            IntegrationEvent integrationEvent = new IntegrationEvent("KYC_BATCH_READY", jsonPayload);
            applicationEventPublisher.publishEvent(integrationEvent);

        } catch (Exception e) {
            log.error("Failed to serialize batch ready event", e);
        }
    }
}
