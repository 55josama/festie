package com.ojosama.eventservice.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT1M")
public class ShedLockConfig {
    private static final String SHEDLOCK_TABLE = "event_schema.shedlock";

    @Bean
    public LockProvider lockProvider(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS event_schema");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS event_schema.shedlock (
                    name VARCHAR(64) NOT NULL PRIMARY KEY,
                    lock_until TIMESTAMP NOT NULL,
                    locked_at TIMESTAMP NOT NULL,
                    locked_by VARCHAR(255) NOT NULL
                )
                """);

        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(jdbcTemplate)
                        .withTableName(SHEDLOCK_TABLE)
                        .usingDbTime()
                        .build());
    }
}
