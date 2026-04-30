package com.ojosama.favoriteservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
@ComponentScan(basePackages = "com.ojosama")
@EntityScan(basePackages = {
        "com.ojosama.favoriteservice.domain.model",
        "com.ojosama.common.kafka.domain"
})
@EnableJpaRepositories(basePackages = {
        "com.ojosama.favoriteservice.infrastructure.persistence",
        "com.ojosama.common.kafka.domain"
})
public class FavoriteServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FavoriteServiceApplication.class, args);
    }

}
