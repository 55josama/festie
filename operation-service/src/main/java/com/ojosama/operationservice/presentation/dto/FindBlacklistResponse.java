package com.ojosama.operationservice.presentation.dto;

import com.ojosama.operationservice.application.dto.result.BlacklistResult;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class FindBlacklistResponse {
    private UUID id;
    private UUID userId;
    private String status;
    private String reason;

    public static FindBlacklistResponse from(BlacklistResult result) {
        return FindBlacklistResponse.builder()
                .id(result.getId())
                .userId(result.getUserId())
                .status(result.getStatus())
                .reason(result.getReason())
                .build();
    }
}
