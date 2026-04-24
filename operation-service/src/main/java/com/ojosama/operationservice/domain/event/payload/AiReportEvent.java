package com.ojosama.operationservice.domain.event.payload;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AiReportEvent {
    private UUID targetId;
    private UUID targetUserId;
    private String targetType;
    private String category;
    private String description;
    private String content;
}
