package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.VerifyEventLocationCommand;
import com.ojosama.chatservice.application.service.EventLocationVerificationService;
import com.ojosama.chatservice.domain.exception.ChatException;
import com.ojosama.chatservice.presentation.dto.request.VerifyEventLocationRequest;
import com.ojosama.chatservice.presentation.dto.response.EventLocationVerificationResponse;
import com.ojosama.common.exception.CommonErrorCode;
import com.ojosama.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "위치 인증 API", description = "행사 위치 근처 인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class EventLocationVerificationController {

    private final EventLocationVerificationService eventLocationVerificationService;

    @Operation(summary = "행사 위치 인증",
            description = "현재 위치가 행사 반경 안인지 확인하고 인증 상태를 저장합니다.")
    @PostMapping("/events/{eventId}/location/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventLocationVerificationResponse>> verifyLocation(
            @PathVariable UUID eventId,
            @Parameter(hidden = true) @AuthenticationPrincipal String userId,
            @Valid @RequestBody VerifyEventLocationRequest request
    ) {
        UUID parsedUserId;
        try {
            parsedUserId = UUID.fromString(userId);
        } catch (Exception e) {
            throw new ChatException(CommonErrorCode.INVALID_REQUEST);
        }

        var result = eventLocationVerificationService.verify(new VerifyEventLocationCommand(
                eventId,
                parsedUserId,
                request.currentLatitude(),
                request.currentLongitude()
        ));
        return ResponseEntity.ok(ApiResponse.success(EventLocationVerificationResponse.from(result)));
    }
}
