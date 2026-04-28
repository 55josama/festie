package com.ojosama.eventservice.event.infrastructure.messaging.kafka;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "kafka.topic")
@Validated
public record KafkaTopicProperties(
        @NotBlank String eventCreated,
        @NotBlank String eventDeleted,
        @NotBlank String eventUpdated,
        @NotBlank String scheduleChanged
) {
}
