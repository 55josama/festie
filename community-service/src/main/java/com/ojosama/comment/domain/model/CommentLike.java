package com.ojosama.comment.domain.model;

import com.ojosama.common.audit.BaseUserEntity;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_comment_like_comment_user", columnNames = {"comment_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentLike extends BaseUserEntity {

    @Id
    private UUID id;

    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Builder
    public CommentLike(UUID id, UUID commentId, UUID userId) {
        this.id = Objects.requireNonNull(id, "id 필수");
        this.commentId = Objects.requireNonNull(commentId, "commentId 필수");
        this.userId = Objects.requireNonNull(userId, "userId 필수");
    }
}
