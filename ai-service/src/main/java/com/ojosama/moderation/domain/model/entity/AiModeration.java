package com.ojosama.moderation.domain.model.entity;

import com.ojosama.moderation.domain.model.enums.ReportCategory;
import com.ojosama.moderation.domain.model.enums.TargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Getter
@Table(name = "p_ai_moderation_log", schema = "ai_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiModeration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder
    public AiModeration(TargetType targetType, UUID targetId, UUID targetUserId,
                        String content, ReportCategory category) {
        this.targetId = targetId;
        this.targetUserId = targetUserId;
        this.targetType = targetType;
        this.category = category;
        this.content = content;
    }

    public static AiModeration of(UUID targetId, UUID targetUserId, TargetType targetType,
                                  ReportCategory category, String content){
        return AiModeration.builder()
                .targetId(targetId)
                .targetUserId(targetUserId)
                .targetType(targetType)
                .category(category)
                .content(content)
                .build();
    }
}
