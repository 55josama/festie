package com.ojosama.calendarservice.calendar.presentaion;

import com.ojosama.calendarservice.calendar.application.CalendarService;
import com.ojosama.calendarservice.calendar.presentaion.dto.CalendarResponseDto;
import com.ojosama.calendarservice.calendar.presentaion.dto.CreateCalendarRequestDto;
import com.ojosama.calendarservice.calendar.presentaion.dto.UpdateMemoCalendarRequestDto;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    @PostMapping
    public ResponseEntity<ApiResponse<CalendarResponseDto>> createCalendar(
            @Valid @RequestBody CreateCalendarRequestDto requestDto) {
        // TODO : userId 수정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        CalendarResponseDto dto = calendarService.createCalendar(requestDto.toCommand(userId));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(dto));
    }

    @GetMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<CalendarResponseDto>> getCalendar(@PathVariable UUID calendarId) {
        // TODO : userId 수정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        CalendarResponseDto dto = calendarService.getCalendar(calendarId, userId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarResponseDto>>> getCalendars(
            @RequestParam("year") @Min(2000) @Max(9999) int year,
            @RequestParam("month") @Min(1) @Max(12) int month) {
        // TODO : userId 수정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        List<CalendarResponseDto> list = calendarService.getCalendars(userId, year, month);

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    // TODO : 일정을 바꾸는 ...
    @PatchMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<CalendarResponseDto>> updateMemoCalendars(@PathVariable UUID calendarId,
                                                                                @RequestBody UpdateMemoCalendarRequestDto dto) {
        // TODO : userId 수정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        CalendarResponseDto responseDto = calendarService.updateCalendarMemo(calendarId, dto.memo(), userId);

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @DeleteMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<Void>> deleteCalendar(@PathVariable UUID calendarId) {

        // TODO : userId 수정
        UUID userId = UUID.fromString("bd4e3ba4-55dd-45d4-b1ca-55f38f0c4804");

        calendarService.deleteCalendar(calendarId, userId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

}
