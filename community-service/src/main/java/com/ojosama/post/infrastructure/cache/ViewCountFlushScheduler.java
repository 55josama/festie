package com.ojosama.post.infrastructure.cache;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Redis 에 쌓인 조회수를 주기적으로 DB 에 flush 하는 스케줄러.
 *
 * 흐름: peek(Redis 조회) → flushOne(DB UPDATE, REQUIRES_NEW) → resetBy(Redis 차감)
 * DB UPDATE 성공 후에 Redis 를 차감하므로 중간에 장애가 나도 조회수가 유실되지 않는다.
 * 전체 루프에 @Transactional 을 걸지 않고 flushOne 단위로 트랜잭션을 분리해,
 * 한 글 실패가 다른 글 flush 에 영향을 주지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountFlushScheduler {

    @Value("${community.view-count.batch-size:500}")
    private int batchSize;

    private final ViewCountCacheService viewCountCache;
    private final PostViewCountExecutor postViewCountExecutor;

    @Scheduled(fixedDelayString = "${community.view-count.flush-interval-ms:30000}")
    public void flush() {
        Set<String> dirtyIds = viewCountCache.getDirtyPostIds();
        if (dirtyIds.isEmpty()) {
            return;
        }

        // 한 번에 처리할 글 수를 제한해 DB 락 점유 시간을 제어
        List<String> batch = dirtyIds.stream()
                .limit(batchSize)
                .toList();

        int flushedCount = 0;
        long totalDelta = 0L;

        for (String idStr : batch) {
            UUID postId;
            try {
                postId = UUID.fromString(idStr);
            } catch (IllegalArgumentException e) {
                log.warn("[ViewCountFlush] 잘못된 UUID, dirty set 에서 제거: {}", idStr);
                viewCountCache.removeInvalidEntry(idStr);
                continue;
            }

            try {
                long delta = viewCountCache.peek(postId);
                if (delta <= 0) {
                    viewCountCache.discard(postId);
                    continue;
                }

                boolean updated = postViewCountExecutor.flushOne(postId, delta);

                if (updated) {
                    viewCountCache.resetBy(postId, delta);
                    flushedCount++;
                    totalDelta += delta;
                } else {
                    // affected=0: 글이 삭제된 케이스
                    viewCountCache.discard(postId);
                }

            } catch (Exception e) {
                // DB 장애 등 — dirty 유지, 다음 주기에 재시도
                log.error("[ViewCountFlush] flush 실패, 다음 주기 재시도 postId={}: {}", postId, e.getMessage());
            }
        }

        if (flushedCount > 0) {
            log.info("[ViewCountFlush] {}개 글 flush 완료 (총 조회수 +{})", flushedCount, totalDelta);
        }
        if (dirtyIds.size() > batchSize) {
            log.debug("[ViewCountFlush] dirty {}개 중 {}개 처리, 나머지 다음 주기", dirtyIds.size(), batch.size());
        }
    }
}
