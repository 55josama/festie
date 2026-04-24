package com.ojosama.operationservice.domain.model.entity;

import com.ojosama.common.audit.BaseEntity;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportStatus;
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
@Table(name = "p_report", schema = "operation_service")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporter_type", nullable = false)
    private ReporterType reporterType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ReportCategory category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportStatus status;

    @Column(name = "operator_memo")
    private String operatorMemo;

    @Builder
    public Report(UUID reporterId, ReporterType reporterType, UUID targetId, ReportTargetType targetType,
                  ReportCategory category, String description, String content) {
        this.reporterId = reporterId;
        this.reporterType = reporterType; // 생성 시 주입
        this.targetId = targetId;
        this.targetType = targetType;
        this.category = category;
        this.description = description;
        this.content = content;
        this.status = ReportStatus.PENDING;
    }

    public static Report of(UUID reporterId, ReporterType reporterType, UUID targetId, ReportTargetType targetType,
                            ReportCategory category, String description, String content) {
        return Report.builder()
                .reporterId(reporterId)
                .reporterType(reporterType)
                .targetId(targetId)
                .targetType(targetType)
                .category(category)
                .description(description)
                .content(content)
                .build();
    }

    public void resolve(String operatorMemo) {
        this.status = ReportStatus.RESOLVED;
        this.operatorMemo = operatorMemo;
    }

    public void reject(String operatorMemo) {
        this.status = ReportStatus.REJECTED;
        this.operatorMemo = operatorMemo;
    }
}
