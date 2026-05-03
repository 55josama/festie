package com.ojosama.chatservice;

import com.ojosama.common.kafka.domain.IdempotentEventHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
})
@Import(IdempotentEventHandler.class)
@EnableFeignClients(basePackages = "com.ojosama")
@ComponentScan(basePackages = "com.ojosama")
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

}
