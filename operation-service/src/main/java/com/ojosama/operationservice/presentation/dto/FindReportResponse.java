package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.ReportInfoResult;
import java.util.UUID;

public record FindReportResponse (
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
    public static FindReportResponse from(ReportInfoResult result) {
        return new FindReportResponse(
                result.id(),
                result.reporterId(),
                result.reporterType(),
                result.targetId(),
                result.targetType(),
                result.category(),
                result.description(),
                result.content(),
                result.status(),
                result.operatorMemo()
        );
    }
}
