package com.ojosama;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableFeignClients(basePackages = "com.ojosama")
@SpringBootApplication
public class OperationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OperationServiceApplication.class, args);
    }

}
