package com.ojosama.eventservice.event.infrastructure.messaging.kafka;

import com.ojosama.common.config.kafka.KafkaConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaConfig.class)
public class EventKafkaConfig {
}
