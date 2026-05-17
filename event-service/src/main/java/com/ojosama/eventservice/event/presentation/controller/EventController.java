package com.ojosama.eventservice.event.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.eventservice.event.application.dto.command.CreateEventCommand;
import com.ojosama.eventservice.event.application.dto.command.EventListCommand;
import com.ojosama.eventservice.event.application.dto.command.UpdateEventCommand;
import com.ojosama.eventservice.event.application.dto.result.EventListResult;
import com.ojosama.eventservice.event.application.dto.result.EventResult;
import com.ojosama.eventservice.event.application.service.EventCommandService;
import com.ojosama.eventservice.event.application.service.EventQueryService;
import com.ojosama.eventservice.event.domain.model.EventStatus;
import com.ojosama.eventservice.event.presentation.dto.request.CreateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.request.UpdateEventRequest;
import com.ojosama.eventservice.event.presentation.dto.response.EventDetailResponse;
import com.ojosama.eventservice.event.presentation.dto.response.EventListResponse;
import com.ojosama.eventservice.event.presentation.dto.response.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/v1/events")
@Tag(name = "행사 API", description = "행사 등록, 조회, 수정, 삭제")
public class EventController {

    private final EventCommandService eventCommandService;
    private final EventQueryService eventQueryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "행사 등록", description = "매니저/관리자 전용 API입니다. 행사를 등록하면 SCHEDULED 상태로 생성됩니다. 티켓팅이 있는 경우 티켓팅 시작일, 종료일, 링크를 모두 입력해야 합니다. 세부 일정(schedules)은 최소 1개 이상 필수입니다.")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateEventRequest request) {

        EventResult result = eventCommandService.createEvent(CreateEventCommand.from(UUID.fromString(userId), request));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(EventResponse.from(result)));
    }

    @GetMapping
    @Operation(summary = "행사 목록 조회", description = "카테고리(category), 상태(status), 날짜 범위(startAt, endAt), 연도/월(year, month)로 필터링할 수 있습니다. 필터를 여러 개 조합할 수 있으며, 기본 정렬은 시작일 오름차순입니다. 삭제된 행사는 목록에 포함되지 않습니다.")
    public ResponseEntity<ApiResponse<Page<EventListResponse>>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @PageableDefault(size = 10, sort = "eventTime.startAt", direction = Sort.Direction.ASC) Pageable pageable) {

        EventListCommand command = new EventListCommand(category, status, startAt, endAt, year, month);
        Page<EventListResult> result = eventQueryService.getEvents(command, pageable);
        return ResponseEntity.ok(ApiResponse.success(result.map(EventListResponse::from)));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "행사 상세 조회", description = "행사의 세부 정보와 함께 연결된 채팅방 정보를 반환합니다. 채팅 서비스와 연동하여 채팅방 상태, 오픈/종료 일정 등을 포함합니다. 채팅 서비스 장애 시에도 행사 정보는 정상 반환되며, 채팅방 정보는 chatRoomExists: false로 응답됩니다.")
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEvent(
            @PathVariable UUID eventId) {

        EventResult result = eventQueryService.getEventById(eventId);
        return ResponseEntity.ok(ApiResponse.success(EventDetailResponse.from(result)));
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "행사 수정", description = "매니저/관리자 전용 API입니다. 행사의 모든 필드를 수정할 수 있습니다. 세부 일정(schedules)을 요청에 포함하면 기존 일정 전체가 교체되며, 생략하면 기존 일정이 그대로 유지됩니다. 수정된 내용은 Kafka를 통해 연관 서비스에 전파됩니다.")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody UpdateEventRequest request) {

        EventResult result = eventCommandService.updateEvent(UpdateEventCommand.from(eventId, UUID.fromString(userId), request));
        return ResponseEntity.ok(ApiResponse.success(EventResponse.from(result)));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "행사 삭제", description = "매니저/관리자 전용 API입니다. 실제 데이터를 제거하지 않고 삭제된 것으로 처리하는 소프트 딜리트 방식입니다. 삭제 후 목록 및 상세 조회에서 노출되지 않으며, 삭제 이벤트가 Kafka를 통해 연관 서비스에 전파됩니다.")
    public ResponseEntity<Void> deleteEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID eventId) {

        eventCommandService.deleteEvent(UUID.fromString(userId), eventId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONCERT_MANAGER', 'FESTIVAL_MANAGER', 'FANMEETING_MANAGER', 'POPUP_MANAGER')")
    @Operation(summary = "행사 취소", description = "매니저/관리자 전용 API입니다. SCHEDULED(예정) 또는 IN_PROGRESS(진행 중) 상태의 행사만 취소할 수 있습니다. 취소 시 아직 실행되지 않은 예약 액션도 함께 취소되며, 취소 이벤트가 Kafka를 통해 연관 서비스에 전파됩니다.")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @PathVariable UUID eventId) {

        EventResult result = eventCommandService.cancelEvent(eventId, UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(EventResponse.from(result)));
    }
}
