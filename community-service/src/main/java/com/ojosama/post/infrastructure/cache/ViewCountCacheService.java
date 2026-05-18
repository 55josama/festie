package com.ojosama.post.infrastructure.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountCacheService {

    private static final String VIEW_KEY_PREFIX = "post:views:";
    private static final String DIRTY_SET_KEY = "post:views:dirty";

    /**
     * DECRBY + 조건부 SADD 를 원자적으로 실행하는 Lua 스크립트.
     *
     * SPOP 으로 dirty set 에서 이미 제거된 상태이므로 SREM 은 불필요.
     * DECRBY 후 remaining > 0 이면 flush 중 새 INCR 가 들어온 것 → dirty set 에 재등록.
     *
     * KEYS[1] = viewKey(postId), KEYS[2] = DIRTY_SET_KEY
     * ARGV[1] = flushed, ARGV[2] = postId string
     */
    private static final RedisScript<Long> RESET_BY_LUA = RedisScript.of(
        "local remaining = redis.call('DECRBY', KEYS[1], ARGV[1])\n" +
        "if tonumber(remaining) > 0 then\n" +
        "    redis.call('SADD', KEYS[2], ARGV[2])\n" +
        "end\n" +
        "return remaining",
        Long.class
    );

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

    /**
     * dirty set 에서 최대 count 개를 원자적으로 SPOP.
     *
     * SMEMBERS(읽기만) 대신 SPOP(꺼내기)를 사용하므로 멀티 인스턴스 환경에서
     * 동일 postId 를 두 인스턴스가 중복 처리하는 문제를 방지한다.
     */
    public Set<String> spopDirtyPostIds(int count) {
        try {
            // Spring Data Redis 3.x 에서 pop(key, count) 는 List<V> 반환
            List<String> popped = redis.opsForSet().pop(DIRTY_SET_KEY, count);
            return popped != null ? new HashSet<>(popped) : Set.of();
        } catch (Exception e) {
            log.error("[ViewCountCache] dirty set SPOP 실패: {}", e.getMessage());
            return Set.of();
        }
    }

    // DB 장애 등으로 flush 실패 시 다음 주기에 재시도할 수 있도록 dirty set 에 재등록
    public void requeue(UUID postId) {
        try {
            redis.opsForSet().add(DIRTY_SET_KEY, postId.toString());
        } catch (Exception e) {
            log.warn("[ViewCountCache] requeue 실패 postId={}", postId);
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

    /**
     * DB UPDATE 성공 후 flush 한 만큼만 차감 — Lua 스크립트로 원자 처리.
     * SPOP 으로 이미 dirty set 에서 제거된 상태이므로 SREM 은 없음.
     * remaining > 0 이면 flush 중 새 INCR 가 들어온 것 → 자동으로 dirty 재등록.
     */
    public void resetBy(UUID postId, long flushed) {
        if (flushed <= 0) return;
        try {
            redis.execute(
                RESET_BY_LUA,
                List.of(viewKey(postId), DIRTY_SET_KEY),
                String.valueOf(flushed), postId.toString()
            );
        } catch (Exception e) {
            log.error("[ViewCountCache] resetBy 실패 postId={}, flushed={}: {}", postId, flushed, e.getMessage());
        }
    }

    // 글 삭제 등으로 재시도가 불필요한 경우 카운터 키 제거 (dirty set 은 SPOP 으로 이미 제거됨)
    public void discard(UUID postId) {
        try {
            redis.delete(viewKey(postId));
        } catch (Exception e) {
            log.warn("[ViewCountCache] discard 실패 postId={}", postId);
        }
    }

    private String viewKey(UUID postId) {
        return VIEW_KEY_PREFIX + postId;
    }
}
