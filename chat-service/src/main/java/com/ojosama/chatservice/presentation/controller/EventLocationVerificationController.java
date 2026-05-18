package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.command.VerifyEventLocationCommand;
import com.ojosama.chatservice.application.service.EventLocationVerificationService;
import com.ojosama.chatservice.presentation.dto.request.VerifyEventLocationRequest;
import com.ojosama.chatservice.presentation.dto.response.EventLocationVerificationResponse;
import com.ojosama.common.response.ApiResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class EventLocationVerificationController {

    private final EventLocationVerificationService eventLocationVerificationService;

    @PostMapping("/events/{eventId}/location/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventLocationVerificationResponse>> verifyLocation(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody VerifyEventLocationRequest request
    ) {
        var result = eventLocationVerificationService.verify(new VerifyEventLocationCommand(
                eventId,
                UUID.fromString(userId),
                request.currentLatitude(),
                request.currentLongitude()
        ));
        return ResponseEntity.ok(ApiResponse.success(EventLocationVerificationResponse.from(result)));
    }
}
