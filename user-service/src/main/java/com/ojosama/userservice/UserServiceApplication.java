package com.ojosama.userservice;

import com.ojosama.userservice.global.security.InternalApiProperties;
import com.ojosama.userservice.global.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, InternalApiProperties.class})
@ComponentScan(basePackages = "com.ojosama")
@EntityScan(basePackages = {
        "com.ojosama.userservice.domain.model",
        "com.ojosama.common.kafka.domain"
})
@EnableJpaRepositories(basePackages = {
        "com.ojosama.userservice.domain.repository",
        "com.ojosama.common.kafka.domain"
})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
