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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    //유저 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponseDto createUser(@RequestBody CreateUserRequestDto request) {
        CreateUserResult result = userService.createUser(request.toCommand());

        return CreateUserResponseDto.from(result);
    }

    //유저 단건 조회
    @GetMapping("/{userId}")
    public GetUserResponseDto getUser(@PathVariable UUID userId) {
        GetUserQuery query = new GetUserQuery(userId);

        GetUserResult result = userService.getUser(query);

        return GetUserResponseDto.from(result);
    }

    //유저 수정
    @PatchMapping("/{userId}")
    public UpdateUserResponseDto updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequestDto request) {
        UpdateUserResult result = userService.updateUser(request.toCommand(userId));
        return UpdateUserResponseDto.from(result);
    }

    //유저 삭제
    @DeleteMapping
    private void deleteUser(
            @PathVariable UUID userId
    ) {
        DeleteUserCommand command = new DeleteUserCommand(userId);

        userService.deleteUser(command);
    }
}
