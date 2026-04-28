package com.ojosama.chatservice;

import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import com.ojosama.common.kafka.domain.OutboxRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EntityScan(basePackages = {
        "com.ojosama.chatservice.domain.model",
        "com.ojosama.common.kafka.domain"
})
@EnableJpaRepositories(basePackages = {
        "com.ojosama.chatservice.infrastructure.persistence",
        "com.ojosama.common.kafka.domain"
}, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OutboxRepository.class))
@Import(IdempotentEventHandler.class)
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

}
