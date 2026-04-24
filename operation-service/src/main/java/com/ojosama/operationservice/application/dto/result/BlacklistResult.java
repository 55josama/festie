package com.ojosama.operationservice.application.dto.result;

import com.ojosama.operationservice.domain.model.entity.Blacklist;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class BlacklistResult {
    private UUID id;
    private UUID userId;
    private String status;
    private String reason;

    public static BlacklistResult from(Blacklist blacklist) {
        return BlacklistResult.builder()
                .id(blacklist.getId())
                .userId(blacklist.getUserId())
                .status(blacklist.getStatus().name())
                .reason(blacklist.getReason())
                .build();
    }
}
