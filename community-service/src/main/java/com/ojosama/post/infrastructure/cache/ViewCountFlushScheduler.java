package com.ojosama.post.infrastructure.cache;

import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 에 쌓인 조회수를 주기적으로 DB 에 flush 하는 스케줄러.
 * 흐름: spopDirtyPostIds(SPOP) → peek → flushOne(DB UPDATE) → resetBy(Redis 차감)
 * SMEMBERS 대신 SPOP 을 사용하는 이유:
 *   멀티 인스턴스 환경에서 SMEMBERS 는 모든 인스턴스가 동일 postId 를 읽어
 *   동일 delta 를 DB 에 중복 flush 할 수 있다. SPOP 은 pop 이 원자적이라
 *   인스턴스 간 같은 postId 를 중복으로 처리하지 않는다.
 * DB 실패 시: requeue() 로 dirty set 에 재등록 → 다음 주기에 재시도
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountFlushScheduler {

    @Value("${community.view-count.batch-size:100}")
    private int batchSize;

    private final ViewCountCacheService viewCountCache;
    private final PostViewCountExecutor postViewCountExecutor;

    @Scheduled(fixedDelayString = "${community.view-count.flush-interval-ms:30000}")
    public void flush() {
        // SPOP: 최대 batchSize 개를 원자적으로 꺼냄 (다른 인스턴스와 중복 없음)
        Set<String> poppedIds = viewCountCache.spopDirtyPostIds(batchSize);
        if (poppedIds.isEmpty()) {
            return;
        }

        int flushedCount = 0;
        long totalDelta  = 0L;

        for (String idStr : poppedIds) {
            UUID postId;
            try {
                postId = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                // 잘못된 UUID — SPOP 으로 이미 dirty set 에서 제거됐으므로 그냥 skip
                log.warn("[ViewCountFlush] 잘못된 UUID, skip: {}", idStr);
                continue;
            }

            try {
                long delta = viewCountCache.peek(postId);
                if (delta <= 0) {
                    // 이미 다른 경로에서 처리됐거나 값이 없는 경우 — Redis 키만 정리
                    viewCountCache.discard(postId);
                    continue;
                }

                boolean updated = postViewCountExecutor.flushOne(postId, delta);

                if (updated) {
                    // DB UPDATE 성공 → Redis 에서 flush 한 만큼 차감 (INCR 잔여분 보존)
                    viewCountCache.resetBy(postId, delta);
                    flushedCount++;
                    totalDelta += delta;
                } else {
                    // affected=0: 글이 삭제된 경우 — Redis 키 정리, 재시도 불필요
                    viewCountCache.discard(postId);
                }

            } catch (Exception e) {
                // DB 장애 등 — dirty set 에 재등록해 다음 주기에 재시도
                log.error("[ViewCountFlush] flush 실패, 다음 주기 재시도 postId={}: {}", postId, e.getMessage());
                viewCountCache.requeue(postId);
            }
        }

        if (flushedCount > 0) {
            log.info("[ViewCountFlush] {}개 글 flush 완료 (총 조회수 +{})", flushedCount, totalDelta);
        }
    }
}
