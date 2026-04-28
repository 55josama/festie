package com.ojosama.calendarservice.calendar.presentaion.dto;

import jakarta.validation.constraints.Size;

public record UpdateMemoCalendarRequestDto(
        @Size(max = 1000)
        String memo
) {


}
