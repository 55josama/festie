package com.ojosama.report.application.dto.result;

import com.ojosama.report.domain.model.entity.Report;
import java.util.UUID;

public record ReportResult (
        UUID id,
        UUID reporterId,
        String reporterType,
        UUID targetId,
        UUID targetUserId,
        String targetType,
        String category,
        String description,
        String targetContent,
        String status,
        String operatorMemo
){
    public static ReportResult from(Report report) {
        return new ReportResult(
                report.getId(),
                report.getReporterId(),
                report.getReporterType().name(),
                report.getTargetId(),
                report.getTargetUserId(),
                report.getTargetType().name(),
                report.getCategory().name(),
                report.getDescription(),
                report.getContent(),
                report.getStatus().name(),
                report.getOperatorMemo()
        );
    }
}
