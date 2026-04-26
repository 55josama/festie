package com.ojosama.operationservice.application.dto.result;

import com.ojosama.operationservice.domain.model.entity.Report;
import java.util.UUID;

public record ReportResult (
        UUID id,
        UUID reporterId,
        String reporterType,
        UUID targetId,
        String targetType,
        String category,
        String description,
        String status,
        String operatorMemo
){
    public static ReportResult from(Report report) {
        return new ReportResult(
                report.getId(),
                report.getReporterId(),
                report.getReporterType().name(),
                report.getTargetId(),
                report.getTargetType().name(),
                report.getCategory().name(),
                report.getDescription(),
                report.getStatus().name(),
                report.getOperatorMemo()
        );
    }
}
