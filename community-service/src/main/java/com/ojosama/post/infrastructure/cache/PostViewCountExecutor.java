package com.ojosama.post.infrastructure.cache;

import com.ojosama.post.domain.repository.PostRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 조회수 DB 쓰기를 REQUIRES_NEW 트랜잭션으로 분리.
 *
 * Spring AOP self-invocation 문제로 인해 Scheduler/Service 내부 메서드로 분리해도
 * REQUIRES_NEW 가 적용되지 않아 별도 빈으로 추출했다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewCountExecutor {

    private final PostRepository postRepository;

    /**
     * @return true: UPDATE 성공 / false: affected=0 (글 삭제 등, 재시도 불필요)
     * @throws Exception DB 장애 — 호출부에서 dirty 유지 후 다음 주기 재시도
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean flushOne(UUID postId, long delta) {
        int affected = postRepository.incrementViewCountBy(postId, delta);
        if (affected == 0) {
            log.warn("[ViewCountExecutor] UPDATE affected=0 postId={}, delta={}", postId, delta);
            return false;
        }
        return true;
    }

}
