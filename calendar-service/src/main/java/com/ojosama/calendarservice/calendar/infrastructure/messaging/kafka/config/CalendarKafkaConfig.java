package com.ojosama.calendarservice.calendar.infrastructure.messaging.kafka.config;

import com.ojosama.common.config.kafka.KafkaConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(KafkaConfig.class)
public class CalendarKafkaConfig {
}