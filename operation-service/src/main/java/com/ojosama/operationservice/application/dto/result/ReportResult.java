package com.ojosama.operationservice.application.dto.result;

import com.ojosama.operationservice.domain.model.entity.Report;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReportResult {
    private UUID id;
    private UUID reporterId;
    private String reporterType;
    private UUID targetId;
    private String targetType;
    private String category;
    private String description;
    private String status;
    private String operatorMemo;

    public static ReportResult from(Report report) {
        return ReportResult.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .reporterType(report.getReporterType().name())
                .targetId(report.getTargetId())
                .targetType(report.getTargetType().name())
                .category(report.getCategory().name())
                .description(report.getDescription())
                .status(report.getStatus().name())
                .operatorMemo(report.getOperatorMemo())
                .build();
    }
}
