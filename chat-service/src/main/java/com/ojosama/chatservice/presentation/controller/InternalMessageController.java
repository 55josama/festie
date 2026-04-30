package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.result.ReportedMessageResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.presentation.dto.response.ReportedMessageResponse;
import com.ojosama.common.response.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/chat/messages")
public class InternalMessageController {

    private final MessageService messageService;

    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse<ReportedMessageResponse>> getReportedMessage(
            @PathVariable UUID messageId
    ) {
        ReportedMessageResult result = messageService.getReportedMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success(ReportedMessageResponse.from(result)));
    }
}
