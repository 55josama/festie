package com.ojosama.calendarservice.calendar.infrastructure.config;

import com.ojosama.common.config.kafka.KafkaConfig;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@Import(KafkaConfig.class)
@ComponentScan(basePackages = "com.ojosama.common.kafka.domain")
@EntityScan(basePackages = {"com.ojosama.calendarservice", "com.ojosama.common.kafka.domain"})
@EnableJpaRepositories(basePackages = {"com.ojosama.calendarservice", "com.ojosama.common.kafka.domain"})
public class CalendarKafkaConfig {
}
