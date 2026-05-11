package com.ojosama.calendarservice.calendar.presentation.dto.request;

import com.ojosama.calendarservice.calendar.application.dto.command.UpdateCalendarCommand;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateMemoCalendarRequestDto(
        @Size(max = 1000)
        String memo
) {
    public UpdateCalendarCommand toCommand(UUID userID, UUID calendarId) {
        return UpdateCalendarCommand.builder()
                .memo(this.memo)
                .userId(userID)
                .calendarId(calendarId)
                .build();
    }
}
