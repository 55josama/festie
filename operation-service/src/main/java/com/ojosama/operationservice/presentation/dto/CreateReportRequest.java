package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.command.CreateReportCommand;
import com.ojosama.operationservice.domain.model.enums.ReportCategory;
import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateReportRequest (
        @NotNull(message = "신고 대상 ID를 입력해주세요.")
        UUID targetId,

        @NotNull(message = "신고 대상 타입을 입력해주세요.")
        ReportTargetType targetType,

        @NotNull(message = "신고 카테고리를 선택해주세요.")
        ReportCategory category,

        @NotBlank(message = "신고 상세 사유를 입력해주세요.")
        @Size(min = 5, max = 1000, message = "신고 사유는 5자 이상, 1000자 이하로 작성해주세요.")
        String description,

        @NotBlank(message = "신고 내용을 입력해주세요.")
        String content
){
    public CreateReportCommand toCommand(UUID reporterId, UUID targetUserId) {
        return new CreateReportCommand(reporterId, targetId, targetUserId, targetType, category, description, content);
    }
}
