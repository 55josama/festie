package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.dto.query.GetCategoryManagerQuery;
import com.ojosama.userservice.application.dto.query.GetInternalUserEmailQuery;
import com.ojosama.userservice.application.dto.result.GetAdminIdResult;
import com.ojosama.userservice.application.dto.result.GetCategoryManagerIdResult;
import com.ojosama.userservice.application.dto.result.InternalUserEmailResult;
import com.ojosama.userservice.application.service.UserService;
import com.ojosama.userservice.presentation.dto.response.InternalAdminIdResponseDto;
import com.ojosama.userservice.presentation.dto.response.InternalManagerIdResponseDto;
import com.ojosama.userservice.presentation.dto.response.InternalUserEmailResponseDto;
import com.ojosama.userservice.presentation.dto.response.InternalUserEmailsResponseDto;
import com.ojosama.userservice.presentation.dto.response.InternalUserNicknameResponseDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/users")
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{userId}/email")
    public String getInternalUserEmail(@PathVariable UUID userId) {
        InternalUserEmailResult result = userService.getInternalUserEmail(new GetInternalUserEmailQuery(userId));
        return result.email();
    }

    @GetMapping("/{userId}/email/detail")
    public InternalUserEmailResponseDto getInternalUserEmailDetail(@PathVariable UUID userId) {
        InternalUserEmailResult result = userService.getInternalUserEmail(new GetInternalUserEmailQuery(userId));
        return InternalUserEmailResponseDto.from(result);
    }

    @PostMapping("/emails")
    public InternalUserEmailsResponseDto getInternalUserEmails(@RequestBody List<UUID> userIds) {
        return InternalUserEmailsResponseDto.from(userService.getInternalUserEmails(userIds));
    }

    @GetMapping("/admin")
    public UUID getInternalAdminId() {
        GetAdminIdResult result = userService.getInternalAdminId();
        return result.adminId();
    }

    @GetMapping("/admin/detail")
    public InternalAdminIdResponseDto getInternalAdminIdDetail() {
        GetAdminIdResult result = userService.getInternalAdminId();
        return InternalAdminIdResponseDto.from(result);
    }

    @GetMapping("/admins/managers")
    public InternalManagerIdResponseDto getInternalManagerId(@RequestParam String category) {
        GetCategoryManagerIdResult result = userService.getInternalManagerId(
                new GetCategoryManagerQuery(category)
        );
        return InternalManagerIdResponseDto.from(result);
    }

    @GetMapping("/managers")
    public UUID getInternalManagerIdValue(@RequestParam("categoryName") String categoryName) {
        GetCategoryManagerIdResult result = userService.getInternalManagerId(
                new GetCategoryManagerQuery(categoryName)
        );
        return result.managerId();
    }

    @GetMapping("/{userId}/nickname")
    public InternalUserNicknameResponseDto getInternalUserNickname(@PathVariable UUID userId) {
        String nickname = userService.getInternalUserNickname(userId);

        return new InternalUserNicknameResponseDto(nickname);
    }
}
