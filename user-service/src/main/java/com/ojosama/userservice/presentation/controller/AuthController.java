package com.ojosama.userservice.presentation.controller;

import com.ojosama.userservice.application.dto.result.LoginResult;
import com.ojosama.userservice.application.service.AuthService;
import com.ojosama.userservice.presentation.dto.request.LoginRequestDto;
import com.ojosama.userservice.presentation.dto.request.ReissueTokenRequestDto;
import com.ojosama.userservice.presentation.dto.response.LoginResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto requestDto) {
        LoginResult result = authService.login(requestDto.toCommand());

        return LoginResponseDto.from(result);
    }

    @PostMapping("/reissue")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDto reissue(@Valid @RequestBody ReissueTokenRequestDto requestDto) {
        LoginResult result = authService.reissue(requestDto.toCommand());

        return LoginResponseDto.from(result);
    }
}