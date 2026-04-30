package com.ojosama.chatbot.presentation.controller;

import com.ojosama.chatbot.application.service.AiChatService;
import com.ojosama.chatbot.application.service.DocumentIndexer;
import com.ojosama.chatbot.presentation.dto.CreateAiChatRequest;
import com.ojosama.chatbot.presentation.dto.CreateIndexRequest;
import com.ojosama.chatbot.presentation.dto.FindAiChatResponse;
import com.ojosama.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/chatbot")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;
    private final DocumentIndexer documentIndexer;

    @PostMapping
    public ResponseEntity<ApiResponse<FindAiChatResponse>> askQuestion(@Valid @RequestBody CreateAiChatRequest request) {
        FindAiChatResponse response = new FindAiChatResponse(aiChatService.askQuestion(request.question()));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/index")
    public ResponseEntity<ApiResponse<Void>> indexManualDocument(@Valid @RequestBody CreateIndexRequest request) {
        documentIndexer.indexGuide(UUID.randomUUID().toString(), request.content());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(null));
    }
}
