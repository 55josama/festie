package com.ojosama.notificationservice.domain.model.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;
import lombok.AccessLevel;
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

}
