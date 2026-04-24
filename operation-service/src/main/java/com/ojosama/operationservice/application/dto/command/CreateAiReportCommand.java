package com.ojosama.operationservice.application.dto.command;

import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAiReportCommand {
    private UUID targetId;
    private ReportTargetType targetType;
    private ReportCategory category;
    private String description;
}
