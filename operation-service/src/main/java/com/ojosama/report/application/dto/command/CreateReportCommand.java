package com.ojosama.report.application.dto.command;

import com.ojosama.report.domain.model.entity.Report;
import com.ojosama.report.domain.model.enums.ReportCategory;
import com.ojosama.report.domain.model.enums.ReportTargetType;
import com.ojosama.report.domain.model.enums.ReporterType;
import java.util.UUID;

public record CreateReportCommand(
        UUID reporterId,
        UUID targetId,
        UUID targetUserId,
        ReportTargetType targetType,
        ReportCategory category,
        String description,
        String content) {

    public Report toEntity(ReporterType reporterType) {
        return Report.of(
                reporterId,
                reporterType,
                targetId,
                targetUserId,
                targetType,
                category,
                description,
                content
        );
    }
}
