package com.ojosama.post.domain.model;

import com.ojosama.common.audit.BaseUserEntity;
import com.ojosama.common.domain.model.Content;
import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_post",
        indexes = {
                @Index(name = "idx_post_user", columnList = "user_id"),
                @Index(name = "idx_post_category", columnList = "category_id"),
                @Index(name = "idx_post_status_created", columnList = "status, created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseUserEntity {

    private static final int MAX_TITLE_LENGTH = 200;

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Embedded
    private Content content;

    /**
     * 조회 응답용 캐시 카운터. 실제 증감은 native UPDATE로 처리되며,
     * JPA dirty checking으로 변경하지 않는다.
     */
    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.UNVERIFIED;

    /** 본문/제목/카테고리/상태 변경 시에만 영향. 카운터는 native UPDATE라 무관. */
    @Version
    private Long version;

    // ── 생성 ─────────────────────────────────────────────────
    public static Post create(UUID id, UUID userId, UUID categoryId, String title, Content content) {
        validateTitle(title);
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(content, "content must not be null");

        Post post = new Post();
        post.id = Objects.requireNonNull(id, "id must not be null");
        post.userId = userId;
        post.categoryId = categoryId;
        post.title = title;
        post.content = content;
        post.status = PostStatus.UNVERIFIED;
        return post;
    }

    // ── 수정 ─────────────────────────────────────────────────
    /**
     * 게시글 본문 수정. 상태는 UNVERIFIED로 리셋하지 않으며,
     * 본문 수정 시점에는 그대로 유지하고 AI 재검증 이벤트만 발행한다.
     * (사용자 피드백: CLEAN 게시글이 잠시 안 보이는 부작용 방지)
     */
    public void update(String title, Content content, UUID categoryId) {
        validateTitle(title);
        Objects.requireNonNull(categoryId, "categoryId must not be null");
        Objects.requireNonNull(content, "content must not be null");

        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
    }

    // ── 상태 전이 ─────────────────────────────────────────────
    public void markAsClean() {
        this.status = PostStatus.CLEAN;
    }

    /**
     * 게시글 차단. AI 부적절 판정 또는 관리자 차단 또는 작성자 블랙리스트 처리 시 호출.
     */
    public void block() {
        this.status = PostStatus.BLOCKED;
    }

    public boolean isBlocked() {
        return this.status == PostStatus.BLOCKED;
    }

    // ── 인가 헬퍼 ─────────────────────────────────────────────
    public boolean isOwnedBy(UUID userId) {
        return this.userId.equals(userId);
    }

    // ── 내부 검증 ─────────────────────────────────────────────
    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new PostException(PostErrorCode.INVALID_POST_TITLE);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new PostException(PostErrorCode.INVALID_POST_TITLE);
        }
    }
}
