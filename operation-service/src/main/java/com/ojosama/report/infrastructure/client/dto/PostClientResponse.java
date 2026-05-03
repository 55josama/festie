package com.ojosama.report.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PostClientResponse(
        Integer status,
        String message,
        PostData data
) {
    public UUID writerId() {
        return data != null ? data.writerId() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PostData(
            UUID writerId
    ) { }
}
