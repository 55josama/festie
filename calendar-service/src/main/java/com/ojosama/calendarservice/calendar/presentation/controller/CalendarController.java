package com.ojosama.calendarservice.calendar.presentation.controller;

import com.ojosama.calendarservice.calendar.application.CalendarService;
import com.ojosama.calendarservice.calendar.application.dto.command.DeleteCalendarCommand;
import com.ojosama.calendarservice.calendar.application.dto.query.ListCalendarQuery;
import com.ojosama.calendarservice.calendar.presentation.dto.request.CreateCalendarRequestDto;
import com.ojosama.calendarservice.calendar.presentation.dto.request.UpdateMemoCalendarRequestDto;
import com.ojosama.calendarservice.calendar.presentation.dto.response.CalendarResponseDto;
import com.ojosama.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@PreAuthorize("isAuthenticated()")
@RequestMapping("/v1/calendars")
@Tag(name = "캘린더", description = "캘린더 관리 API")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "캘린더 생성", description = "캘린더에 새로운 행사를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CalendarResponseDto>> createCalendar(
            @Valid @RequestBody CreateCalendarRequestDto requestDto,
            @AuthenticationPrincipal UUID userId) {

        CalendarResponseDto dto = calendarService.createCalendar(requestDto.toCommand(userId));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(dto));
    }

    @Operation(summary = "캘린더 행사 상세 조회", description = "캘린더 상세 정보를 조회합니다.")
    @GetMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<CalendarResponseDto>> getCalendar(@PathVariable UUID calendarId,
                                                                        @AuthenticationPrincipal UUID userId) {

        CalendarResponseDto dto = calendarService.getCalendar(calendarId, userId);

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @Operation(summary = "캘린더 행사 조회", description = "캘린더 년/월을 기준으로 행사를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CalendarResponseDto>>> getCalendars(
            @RequestParam("year") @Min(2000) @Max(9999) int year,
            @RequestParam("month") @Min(1) @Max(12) int month,
            @AuthenticationPrincipal UUID userId) {

        List<CalendarResponseDto> list = calendarService.getCalendars(ListCalendarQuery.of(userId, year, month));

        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @Operation(summary = "캘린더 행사 메모 수정", description = "캘린더에 메모를 수정합니다.")
    @PatchMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<CalendarResponseDto>> updateMemoCalendars(@PathVariable UUID calendarId,
                                                                                @RequestBody @Valid UpdateMemoCalendarRequestDto dto,
                                                                                @AuthenticationPrincipal UUID userId) {
        CalendarResponseDto responseDto = calendarService.updateCalendarMemo(
                dto.toCommand(userId, calendarId));

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "캘린더 행사 삭제", description = "삭제하고 싶은 행사를 캘린더에서 삭제합니다.")
    @DeleteMapping("/{calendarId}")
    public ResponseEntity<ApiResponse<Void>> deleteCalendar(@PathVariable UUID calendarId,
                                                            @AuthenticationPrincipal UUID userId) {
        calendarService.deleteCalendar(new DeleteCalendarCommand(calendarId, userId));
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
