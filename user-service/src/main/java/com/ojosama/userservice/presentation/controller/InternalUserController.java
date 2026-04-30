package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.dto.query.GetInternalUserEmailQuery;
import com.ojosama.userservice.application.dto.result.InternalUserEmailResult;
import com.ojosama.userservice.application.service.UserService;
import com.ojosama.userservice.presentation.dto.response.InternalUserEmailResponseDto;
import com.ojosama.userservice.presentation.dto.response.InternalUserEmailsResponseDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}/email")
    public InternalUserEmailResponseDto getInternalUserEmail(@PathVariable UUID userId) {
        InternalUserEmailResult result = userService.getInternalUserEmail(new GetInternalUserEmailQuery(userId));

        return InternalUserEmailResponseDto.from(result);
    }

    @PostMapping("/emails")
    public InternalUserEmailsResponseDto getInternalUserEmails(@RequestBody List<UUID> userIds) {
        return InternalUserEmailsResponseDto.from(
                userService.getInternalUserEmails(userIds)
        );
    }

    @GetMapping("/ping")
    public String ping() {
        return "internal user api ok";
    }
}