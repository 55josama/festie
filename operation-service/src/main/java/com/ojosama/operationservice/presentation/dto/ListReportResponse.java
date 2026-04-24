package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.ReportResult;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ListReportResponse {
    private UUID id;
    private UUID reporterId;
    private String reporterType;
    private UUID targetId;
    private String targetType;
    private String category;
    private String description;
    private String status;
    private String operatorMemo;

    public static ListReportResponse from(ReportResult result) {
        return ListReportResponse.builder()
                .id(result.getId())
                .reporterId(result.getReporterId())
                .reporterType(result.getReporterType())
                .targetId(result.getTargetId())
                .targetType(result.getTargetType())
                .category(result.getCategory())
                .description(result.getDescription())
                .status(result.getStatus())
                .operatorMemo(result.getOperatorMemo())
                .build();
    }
}
