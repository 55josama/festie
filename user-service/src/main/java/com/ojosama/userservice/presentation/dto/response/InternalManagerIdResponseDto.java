package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.GetCategoryManagerIdResult;
import java.util.UUID;

public record InternalManagerIdResponseDto(
        UUID managerId
) {
    public static InternalManagerIdResponseDto from(GetCategoryManagerIdResult result) {
        return new InternalManagerIdResponseDto(result.managerId());
    }
}