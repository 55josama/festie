package com.ojosama.report.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostClientResponse(
        Integer status,
        String message,
        PostData data
) {
    public UUID userId() {
        return data != null ? data.userId() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PostData(
            UUID userId
    ) { }
}
