package com.ojosama.eventservice.event.infrastructure.messaging.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topic")
public record KafkaTopicProperties(
        String eventCreated,
        String eventDeleted,
        String eventUpdated,
        String scheduleChanged
) {
}
