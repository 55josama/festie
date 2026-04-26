package com.ojosama.operationservice.application.dto.command;

import com.ojosama.operationservice.domain.model.entity.Report;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import com.ojosama.operationservice.domain.model.enums.ReporterType;
import java.util.UUID;

public record CreateAiReportCommand(
        UUID reporterId,
        UUID targetId,
        UUID targetUserId,
        ReportTargetType targetType,
        ReportCategory category,
        String description,
        String content) {

    public Report toEntity(ReporterType reporterType){
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
