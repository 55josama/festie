package com.ojosama.chatservice.presentation.controller;

import com.ojosama.chatservice.application.dto.query.FindAdminMessagesQuery;
import com.ojosama.chatservice.application.dto.result.MessageSliceResult;
import com.ojosama.chatservice.application.service.MessageService;
import com.ojosama.chatservice.domain.model.EventCategory;
import com.ojosama.chatservice.domain.model.MessageStatus;
import com.ojosama.chatservice.presentation.dto.response.MessageSliceResponse;
import com.ojosama.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/admin/messages")
public class AdminMessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MessageSliceResponse>> getMessagesForAdmin(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) EventCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MessageSliceResult result = messageService.getMessagesForAdmin(
                new FindAdminMessagesQuery(status, category, page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(MessageSliceResponse.from(result, false)));
    }
}
