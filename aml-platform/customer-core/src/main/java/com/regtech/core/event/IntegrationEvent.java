package com.regtech.core.event;

public record IntegrationEvent(String topic,String jsonPayload) {
}
