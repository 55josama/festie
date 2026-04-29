package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.dto.command.DeleteUserCommand;
import com.ojosama.userservice.application.dto.query.GetUserQuery;
import com.ojosama.userservice.application.dto.result.CreateUserResult;
import com.ojosama.userservice.application.dto.result.GetUserResult;
import com.ojosama.userservice.application.dto.result.UpdateUserResult;
import com.ojosama.userservice.application.service.UserService;
import com.ojosama.userservice.presentation.dto.request.CreateUserRequestDto;
import com.ojosama.userservice.presentation.dto.request.UpdateUserRequestDto;
import com.ojosama.userservice.presentation.dto.response.CreateUserResponseDto;
import com.ojosama.userservice.presentation.dto.response.GetUserResponseDto;
import com.ojosama.userservice.presentation.dto.response.UpdateUserResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponseDto createUser(@Valid @RequestBody CreateUserRequestDto request) {
        CreateUserResult result = userService.createUser(request.toCommand());

        return CreateUserResponseDto.from(result);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public GetUserResponseDto getMyInfo(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        GetUserResult result = userService.getUser(new GetUserQuery(userId));

        return GetUserResponseDto.from(result);
    }

    // 내 정보 수정
    @PatchMapping("/me")
    public UpdateUserResponseDto updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        UpdateUserResult result = userService.updateUser(request.toCommand(userId));

        return UpdateUserResponseDto.from(result);
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyInfo(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        userService.deleteUser(new DeleteUserCommand(userId));
    }
}