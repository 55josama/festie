package com.ojosama.report.infrastructure.lock;

import com.ojosama.report.infrastructure.lock.metrics.LockMetrics;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * Redisson 기반 분산 락 구현
 *
 * Watchdog 자동 갱신 기능을 활성화하여 락 타임아웃 방지
 * - leaseTime을 -1로 설정하면 Watchdog가 자동으로 락을 갱신
 * - 기본 갱신 주기: 30초마다 10초씩 연장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLock {
    private static final String LOCK_PREFIX = "report:lock:";

    private final RedissonClient redissonClient;
    private final LockMetrics lockMetrics;

    /**
     * 분산 락 획득 시도
     *
     * @param targetId 신고 대상 ID
     * @param waitTime 락 획득 대기 시간
     * @param leaseTime 락 보유 시간 (-1이면 Watchdog 활성화)
     * @param unit 시간 단위
     * @return 락 획득 성공 여부
     */
    public boolean tryLock(UUID targetId, long waitTime, long leaseTime, TimeUnit unit)
            throws InterruptedException {

        String lockKey = LOCK_PREFIX + targetId;
        RLock lock = redissonClient.getLock(lockKey);

        long startTime = System.currentTimeMillis();

        // Redisson Fair Lock 사용 (FIFO 순서 보장)
        boolean acquired = lock.tryLock(waitTime, leaseTime, unit);

        long waitDuration = System.currentTimeMillis() - startTime;

        if (acquired) {
            lockMetrics.recordLockWaitTime("redisson", waitDuration);
            lockMetrics.recordLockAcquired("redisson");

            if (leaseTime == -1) {
                log.info("[Redisson] Lock acquired with Watchdog: {} (waited: {}ms)",
                        lockKey, waitDuration);
            } else {
                log.info("[Redisson] Lock acquired: {} (waited: {}ms, lease: {}{})",
                        lockKey, waitDuration, leaseTime, unit.toString().toLowerCase());
            }
        } else {
            lockMetrics.recordLockFailed("redisson");
            log.warn("[Redisson] Lock acquisition failed: {} (waited: {}ms)",
                    lockKey, waitDuration);
        }

        return acquired;
    }

    /**
     * 분산 락 해제
     *
     * @param targetId 신고 대상 ID
     */
    public void unlock(UUID targetId) {
        String lockKey = LOCK_PREFIX + targetId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 현재 스레드가 락을 보유하고 있는 경우에만 해제
            if (lock.isHeldByCurrentThread()) {
                long holdTime = System.currentTimeMillis();
                lock.unlock();
                holdTime = System.currentTimeMillis() - holdTime;

                lockMetrics.recordLockHoldTime("redisson", holdTime);
                log.info("[Redisson] Lock released: {} (held: {}ms)", lockKey, holdTime);
            } else {
                log.warn("[Redisson] Lock not held by current thread: {}", lockKey);
            }
        } catch (IllegalMonitorStateException e) {
            log.error("[Redisson] Failed to unlock (not owner or already released): {}", lockKey, e);
        }
    }

    /**
     * 현재 스레드가 락을 보유하고 있는지 확인
     *
     * @param targetId 신고 대상 ID
     * @return 락 보유 여부
     */
    public boolean isLocked(UUID targetId) {
        String lockKey = LOCK_PREFIX + targetId;
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * 현재 스레드가 락을 보유하고 있는지 확인
     *
     * @param targetId 신고 대상 ID
     * @return 현재 스레드의 락 보유 여부
     */
    public boolean isHeldByCurrentThread(UUID targetId) {
        String lockKey = LOCK_PREFIX + targetId;
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}
