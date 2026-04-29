package com.ojosama.favoriteservice.presentation.dto;

import com.ojosama.favoriteservice.application.dto.command.CreateFavoriteCommand;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CreateFavoriteRequestDto(

        @NotNull(message = "이벤트ID는 필수입니다.")
        UUID eventId,

        @NotNull(message = "카테고리ID는 필수입니다.")
        UUID categoryId
        
) {
    public CreateFavoriteCommand toCommand() {
        return new CreateFavoriteCommand(eventId(), categoryId());
    }
}
