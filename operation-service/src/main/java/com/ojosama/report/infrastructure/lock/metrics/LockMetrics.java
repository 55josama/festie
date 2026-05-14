package com.ojosama.report.infrastructure.lock.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 분산 락 메트릭 수집
 *
 * Prometheus로 다음 메트릭을 수집:
 * - lock_acquired_total: 락 획득 성공 횟수
 * - lock_failed_total: 락 획득 실패 횟수
 * - lock_wait_time_seconds: 락 대기 시간
 * - lock_hold_time_seconds: 락 보유 시간
 */
@Component
@RequiredArgsConstructor
public class LockMetrics {
    private final MeterRegistry meterRegistry;

    // 중복 등록 방지를 위한 캐시
    private final ConcurrentHashMap<String, Counter> counterCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> timerCache = new ConcurrentHashMap<>();

    /**
     * 락 획득 성공 기록
     *
     * @param lockType 락 타입 (redisson)
     */
    public void recordLockAcquired(String lockType) {
        String key = "acquired_" + lockType;
        Counter counter = counterCache.computeIfAbsent(key, k ->
                Counter.builder("lock_acquired_total")
                        .tag("type", lockType)
                        .description("Total number of successful lock acquisitions")
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 락 획득 실패 기록
     *
     * @param lockType 락 타입 (redisson)
     */
    public void recordLockFailed(String lockType) {
        String key = "failed_" + lockType;
        Counter counter = counterCache.computeIfAbsent(key, k ->
                Counter.builder("lock_failed_total")
                        .tag("type", lockType)
                        .description("Total number of failed lock acquisitions")
                        .register(meterRegistry)
        );
        counter.increment();
    }

    /**
     * 락 대기 시간 기록
     *
     * @param lockType 락 타입 (redisson)
     * @param durationMillis 대기 시간 (밀리초)
     */
    public void recordLockWaitTime(String lockType, long durationMillis) {
        String key = "wait_" + lockType;
        Timer timer = timerCache.computeIfAbsent(key, k ->
                Timer.builder("lock_wait_time_seconds")
                        .tag("type", lockType)
                        .description("Time spent waiting to acquire lock")
                        .register(meterRegistry)
        );
        timer.record(durationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 락 보유 시간 기록
     *
     * @param lockType 락 타입 (redisson)
     * @param durationMillis 보유 시간 (밀리초)
     */
    public void recordLockHoldTime(String lockType, long durationMillis) {
        String key = "hold_" + lockType;
        Timer timer = timerCache.computeIfAbsent(key, k ->
                Timer.builder("lock_hold_time_seconds")
                        .tag("type", lockType)
                        .description("Time spent holding the lock")
                        .register(meterRegistry)
        );
        timer.record(durationMillis, TimeUnit.MILLISECONDS);
    }
}
