package com.ojosama.report.infrastructure.lock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "distributed-lock")
public class DistributedLockProperties {
    private long waitTime = 5;      // 기본값: 5초
    private long leaseTime = -1;    // 기본값: -1 (Watchdog 활성화)
}
