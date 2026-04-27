package com.ojosama.favoriteservice.domain.model;

import com.ojosama.common.audit.BaseEntity;
import jakarta.persistence.Column;
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
public class Favorite extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Builder
    private Favorite(UUID userId, UUID eventId, UUID categoryId) {
        this.userId = userId;
        this.eventId = eventId;
        this.categoryId = categoryId;
    }

    public static Favorite of(UUID userId, UUID eventId, UUID categoryId) {
        return Favorite.builder()
                .userId(userId)
                .eventId(eventId)
                .categoryId(categoryId)
                .build();
    }

    public void delete(UUID id) {
        this.deleted();
    }

    public void reset(UUID id) {
        this.undeleted();
    }

}
