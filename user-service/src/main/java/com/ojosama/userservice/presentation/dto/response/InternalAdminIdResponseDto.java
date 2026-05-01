package com.ojosama.userservice.presentation.dto.response;

import com.ojosama.userservice.application.dto.result.GetAdminIdResult;
import java.util.UUID;

public record InternalAdminIdResponseDto(
        UUID adminId
) {
    public static InternalAdminIdResponseDto from(GetAdminIdResult result) {
        return new InternalAdminIdResponseDto(result.adminId());
    }
}