package com.ojosama.operationservice.application.dto.command;

import com.ojosama.operationservice.domain.model.entity.Report;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import com.ojosama.operationservice.domain.model.enums.ReporterType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateReportCommand {
    private UUID reporterId;
    private UUID targetId;
    private UUID targetUserId;
    private ReportTargetType targetType;
    private ReportCategory category;
    private String description;
    private String content;

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
