package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.ReportResult;
import java.util.UUID;

public record ListReportResponse (
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
    public static ListReportResponse from(ReportResult result) {
        return new ListReportResponse(
                result.id(),
                result.reporterId(),
                result.reporterType(),
                result.targetId(),
                result.targetType(),
                result.category(),
                result.description(),
                result.status(),
                result.operatorMemo()
        );
    }
}
