package com.ojosama.favoriteservice.domain.model;

import com.ojosama.common.audit.BaseUserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Getter
@Entity
@Table(name = "p_favorite")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Favorite extends BaseUserEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Embedded
    private EventInfo eventInfo;

    @Column(name = "category_id")
    private UUID categoryId;

    @Builder
    private Favorite(UUID userId, EventInfo eventInfo, UUID categoryId) {
        this.userId = userId;
        this.eventInfo = eventInfo;
        this.categoryId = categoryId;
    }

    public static Favorite of(UUID userId, EventInfo eventInfo, UUID categoryId) {
        return Favorite.builder()
                .userId(userId)
                .eventInfo(eventInfo)
                .categoryId(categoryId)
                .build();
    }

    public void restore(EventInfo eventInfo, UUID categoryId) {
        super.restore();
        this.eventInfo = eventInfo;
        this.categoryId = categoryId;
    }

}
