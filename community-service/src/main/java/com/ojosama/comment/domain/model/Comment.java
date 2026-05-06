package com.ojosama.comment.domain.model;

import com.ojosama.comment.domain.exception.CommentErrorCode;
import com.ojosama.comment.domain.exception.CommentException;
import com.ojosama.common.audit.BaseUserEntity;
import com.ojosama.community.domain.model.Content;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseUserEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(nullable = false)
    private UUID userId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Embedded
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.UNVERIFIED;

    @Column(nullable = false)
    private int likeCount = 0;

    @Version
    private Long version;

    public static Comment createRoot(UUID id, UUID postId, UUID userId, Content content) {
        Comment c = new Comment();
        c.id = Objects.requireNonNull(id, "id must not be null");
        c.postId = Objects.requireNonNull(postId, "postId must not be null");
        c.userId = Objects.requireNonNull(userId, "userId must not be null");
        c.content = Objects.requireNonNull(content, "content must not be null");
        c.parentId = null;
        c.status = CommentStatus.UNVERIFIED;
        return c;
    }

    public static Comment createReply(UUID id, UUID userId, Content content, Comment parent){
        Objects.requireNonNull(parent, "parent must not be null");
        if(parent.parentId != null){
            throw new CommentException(CommentErrorCode.REPLY_DEPTH_EXCEEDED);
        }
        Comment c = new Comment();
        c.id = Objects.requireNonNull(id, "id must not be null");
        c.postId = parent.postId;
        c.userId = Objects.requireNonNull(userId, "userId must not be null");
        c.content = Objects.requireNonNull(content, "content must not be null");
        c.parentId = parent.id;
        c.status = CommentStatus.UNVERIFIED;
        return c;
    }

    public void updateContent(Content content) {
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public boolean isOwnedBy(UUID userId) {
        return this.userId.equals(userId);
    }

    public int increaseLikeCount() {
        this.likeCount++;
        return likeCount;
    }

    public void decreaseLikeCount() {
        if (this.likeCount <= 0) {
            throw new CommentException(CommentErrorCode.LIKE_COUNT_CANNOT_BE_NEGATIVE);
        }
        this.likeCount--;
    }

    public void markAsClean() { this.status = CommentStatus.CLEAN; }
    public void blind() { this.status = CommentStatus.BLINDED; }
}
