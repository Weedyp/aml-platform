package com.regtech.gateway.event;

public interface KycEventPublisher {
    /**
     * Broadcasts an event notifying the system that a batch is ready for KYC screening.
     * * @param event The details of the ingested batch
     */
    void publishBatchReady(KycBatchReadyEvent event);
}
