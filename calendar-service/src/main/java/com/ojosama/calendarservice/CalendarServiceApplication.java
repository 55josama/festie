package com.ojosama.calendarservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
@ComponentScan(basePackages = "com.ojosama")
@EntityScan(basePackages = {
        "com.ojosama.calendarservice.calendar.domain.model",
        "com.ojosama.common.kafka.domain"
})
@EnableJpaRepositories(basePackages = {
        "com.ojosama.calendarservice.calendar.infrastructure.persistence",
        "com.ojosama.common.kafka.domain"
})
public class CalendarServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CalendarServiceApplication.class, args);
    }

}
