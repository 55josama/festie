package com.ojosama.post.infrastructure.cache;

import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountCacheService {

    private static final String VIEW_KEY_PREFIX = "post:views:";
    private static final String DIRTY_SET_KEY = "post:views:dirty";

    private final StringRedisTemplate redis;

    // INCR + SADD 를 pipeline 으로 묶어 RTT 1번에 처리.
    // SessionCallback 은 제네릭 시그니처 문제로 RedisCallback + StringRedisConnection 사용.
    public boolean increment(UUID postId) {
        try {
            final String key = viewKey(postId);
            final String postIdStr = postId.toString();
            redis.executePipelined((RedisCallback<Object>) connection -> {
                StringRedisConnection conn = (StringRedisConnection) connection;
                conn.incr(key);
                conn.sAdd(DIRTY_SET_KEY, postIdStr);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.warn("[ViewCountCache] Redis INCR 실패 postId={} : {}", postId, e.getMessage());
            return false;
        }
    }

    // 스케줄러가 flush 대상 ID 목록을 가져갈 때 사용
    public Set<String> getDirtyPostIds() {
        try {
            Set<String> ids = redis.opsForSet().members(DIRTY_SET_KEY);
            return ids != null ? ids : Set.of();
        } catch (Exception e) {
            log.error("[ViewCountCache] dirty set 조회 실패: {}", e.getMessage());
            return Set.of();
        }
    }

    // 값만 읽고 리셋하지 않음 — DB UPDATE 성공 후 resetBy() 로 차감
    public long peek(UUID postId) {
        try {
            String value = redis.opsForValue().get(viewKey(postId));
            return value != null ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            log.warn("[ViewCountCache] Redis 값 파싱 실패 postId={}", postId);
            return 0L;
        } catch (Exception e) {
            log.error("[ViewCountCache] peek 실패 postId={}: {}", postId, e.getMessage());
            return 0L;
        }
    }

    // DB UPDATE 성공 후 flush 한 만큼만 DECRBY.
    // peek~resetBy 사이에 들어온 INCR 는 그대로 남아 다음 주기에 반영됨.
    // ex) peek=5 → DB +5 → 그 사이 INCR 2번 → resetBy(5) → Redis 잔여=2
    public void resetBy(UUID postId, long flushed) {
        if (flushed <= 0) return;
        try {
            Long remaining = redis.opsForValue().decrement(viewKey(postId), flushed);
            redis.opsForSet().remove(DIRTY_SET_KEY, postId.toString());
            if (remaining != null && remaining > 0) {
                redis.opsForSet().add(DIRTY_SET_KEY, postId.toString());
            }
        } catch (Exception e) {
            log.error("[ViewCountCache] resetBy 실패 postId={}, flushed={}: {}", postId, flushed, e.getMessage());
        }
    }

    // 글 삭제 등으로 재시도가 불필요한 경우 카운터 키와 dirty 항목 모두 제거
    public void discard(UUID postId) {
        try {
            redis.delete(viewKey(postId));
            redis.opsForSet().remove(DIRTY_SET_KEY, postId.toString());
        } catch (Exception e) {
            log.warn("[ViewCountCache] discard 실패 postId={}", postId);
        }
    }

    // dirty set 에 잘못된 UUID 형식이 들어온 경우 제거 (무한 재시도 방지)
    public void removeInvalidEntry(String rawId) {
        try {
            redis.opsForSet().remove(DIRTY_SET_KEY, rawId);
        } catch (Exception e) {
            log.warn("[ViewCountCache] removeInvalidEntry 실패 rawId={}", rawId);
        }
    }

    private String viewKey(UUID postId) {
        return VIEW_KEY_PREFIX + postId;
    }
}
