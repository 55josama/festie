package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.dto.result.CreateUserResult;
import com.ojosama.userservice.application.service.UserService;
import com.ojosama.userservice.presentation.dto.request.CreateUserRequestDto;
import com.ojosama.userservice.presentation.dto.response.CreateUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private UserService userService;

    //유저 생성
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponseDto createUser(@RequestBody CreateUserRequestDto request) {
        CreateUserResult result = userService.createUser(request.toCommand());

        return CreateUserResponseDto.from(result);
    }
}
