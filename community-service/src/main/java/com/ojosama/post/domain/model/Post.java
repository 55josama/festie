package com.ojosama.post.domain.model;

import com.ojosama.category.domain.model.Category;
import com.ojosama.common.audit.BaseEntity;
import com.ojosama.common.domain.model.Content;
import com.ojosama.post.domain.exception.PostErrorCode;
import com.ojosama.post.domain.exception.PostException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID userId; // 작성자 서비스 참조

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String title;

    @Embedded
    private Content content;

    private int viewCount = 0;
    private int likeCount = 0;
    private int commentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.UNVERIFIED;

    public void update(String title, Content content, Category category) {
        this.title = title;
        this.content = content;
        this.category = category;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount <= 0) {
            throw new PostException(PostErrorCode.LIKE_COUNT_CANNOT_BE_NEGATIVE);
        }
        this.likeCount--;
    }

    // ── 조회수 ───────────────────────────────────────────────
    public void increaseViewCount() {
        this.viewCount++;
    }

    // ── 댓글수 ───────────────────────────────────────────────
    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount <= 0) {
            throw new PostException(PostErrorCode.COMMENT_COUNT_CANNOT_BE_NEGATIVE);
        }
        this.commentCount--;
    }

    // ── 상태 변경 ─────────────────────────────────────────────
    public void markAsClean() {
        this.status = PostStatus.CLEAN;
    }

    public void report() {
        this.status = PostStatus.REPORTED;
    }

    public void block() {
        this.status = PostStatus.BLOCKED;
    }
}
