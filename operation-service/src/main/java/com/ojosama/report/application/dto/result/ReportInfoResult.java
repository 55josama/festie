package com.ojosama.report.application.dto.result;

import com.ojosama.report.domain.model.entity.Report;
import java.util.UUID;

public record ReportInfoResult (
        UUID id,
        UUID reporterId,
        String reporterType,
        UUID targetId,
        String targetType,
        String category,
        String description,
        String content,
        String status,
        String operatorMemo
){
    public static ReportInfoResult from(Report report) {
        return new ReportInfoResult(
                report.getId(),
                report.getReporterId(),
                report.getReporterType().name(),
                report.getTargetId(),
                report.getTargetType().name(),
                report.getCategory().name(),
                report.getDescription(),
                report.getContent(),
                report.getStatus().name(),
                report.getOperatorMemo()
        );
    }
}
