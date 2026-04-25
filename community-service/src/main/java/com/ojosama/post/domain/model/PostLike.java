package com.ojosama.post.domain.model;

import com.ojosama.common.audit.BaseEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "p_post_likes",
        uniqueConstraints = {
                // 같은 유저가 같은 게시글에 좋아요를 중복으로 누르는 걸 DB 레벨에서 방지
                @UniqueConstraint(columnNames = {"post_id", "user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseEntity {

    @Id
    private UUID id;

    // Post 전체 객체를 들고 있으면 불필요한 조인이 발생할 수 있어서 단순 FK 참조로만 유지
    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Builder
    public PostLike(UUID id, UUID postId, UUID userId) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
    }
}
