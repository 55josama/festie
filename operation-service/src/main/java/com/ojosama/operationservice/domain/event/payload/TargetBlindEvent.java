package com.ojosama.operationservice.domain.event.payload;

import com.ojosama.operationservice.domain.model.enums.ReportTargetType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record TargetBlindEvent (
        UUID targetId,
        ReportTargetType reportTargetType,
        UUID targetUserId,
        UUID categoryId
){ }
