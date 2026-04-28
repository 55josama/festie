package com.ojosama.notificationservice.domain.model.notification;

import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TargetInfo {

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "target", nullable = false)
    @Enumerated(EnumType.STRING)
    private Target target;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Builder
    private TargetInfo(UUID targetId, Target target, TargetType targetType) {
        validate(targetId, target, targetType);
        this.targetId = targetId;
        this.target = target;
        this.targetType = targetType;
    }

    public static TargetInfo of(UUID targetId, Target target, TargetType targetType) {
        return TargetInfo.builder()
                .targetId(targetId)
                .target(target)
                .targetType(targetType)
                .build();
    }

    public static TargetInfo operation(UUID targetId) {
        return TargetInfo.builder()
                .targetId(targetId)
                .target(Target.OPERATION)
                .targetType(TargetType.REPORT_CREATED)
                .build();
    }

    // 티켓팅 리마인드
    public static TargetInfo ticketing(UUID targetId) {
        return TargetInfo.builder()
                .targetId(targetId)
                .target(Target.EVENT)
                .targetType(TargetType.TICKETING_REMINDER)
                .build();
    }

    // 행사 리마인드
    public static TargetInfo event(UUID targetId) {
        return TargetInfo.builder()
                .targetId(targetId)
                .target(Target.EVENT)
                .targetType(TargetType.EVENT_REMINDER)
                .build();
    }

    private void validate(UUID targetId, Target target, TargetType targetType) {
        if (targetId == null) {
            throw new NotificationException(NotificationErrorCode.INVALID_TARGET_ID);
        }
        if (target == null || targetType == null) {
            throw new NotificationException(NotificationErrorCode.INVALID_TARGET_TYPE);
        }
    }
}
