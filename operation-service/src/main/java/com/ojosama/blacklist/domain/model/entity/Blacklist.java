package com.ojosama.blacklist.domain.model.entity;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.blacklist.domain.model.enums.BlacklistStatus;
import com.ojosama.blacklist.domain.model.enums.RegistrationType;
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

@Entity
@Getter
@Table(name = "p_blacklist", schema = "operation_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Blacklist extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BlacklistStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationType registrationType;

    @Builder
    public Blacklist(UUID userId, String reason, RegistrationType registrationType) {
        this.userId = userId;
        this.reason = reason;
        this.status = BlacklistStatus.ACTIVE;
        this.registrationType = registrationType;
    }

    public static Blacklist of(UUID userId, String reason, RegistrationType registrationType) {
        return Blacklist.builder()
                .userId(userId)
                .reason(reason)
                .registrationType(registrationType)
                .build();
    }

    public void release(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("블랙리스트 해제 사유는 필수입니다.");
        }

        this.status = BlacklistStatus.INACTIVE;
        this.reason = reason;
    }
}
