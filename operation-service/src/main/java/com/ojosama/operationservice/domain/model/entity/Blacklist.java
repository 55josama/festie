package com.ojosama.operationservice.domain.model.entity;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.operationservice.domain.model.enums.BlacklistStatus;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import com.ojosama.operationservice.domain.model.enums.ReporterType;
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

    @Builder
    public Blacklist(UUID userId, String reason) {
        this.userId = userId;
        this.reason = reason;
        this.status = BlacklistStatus.ACTIVE;
    }

    public static Blacklist of(UUID userId, String reason) {
        return Blacklist.builder()
                .userId(userId)
                .reason(reason)
                .build();
    }

    public void release() {
        this.status = BlacklistStatus.INACTIVE;
    }
}
