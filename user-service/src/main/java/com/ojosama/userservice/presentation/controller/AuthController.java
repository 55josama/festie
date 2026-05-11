package com.ojosama.userservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.userservice.application.dto.result.LoginResult;
import com.ojosama.userservice.application.dto.result.LogoutResult;
import com.ojosama.userservice.application.service.AuthService;
import com.ojosama.userservice.presentation.dto.request.LoginRequestDto;
import com.ojosama.userservice.presentation.dto.request.ReissueTokenRequestDto;
import com.ojosama.userservice.presentation.dto.response.LoginResponseDto;
import com.ojosama.userservice.presentation.dto.response.LogoutResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto
    ) {
        LoginResult result = authService.login(requestDto.toCommand());
        LoginResponseDto response = LoginResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<LoginResponseDto>> reissue(
            @Valid @RequestBody ReissueTokenRequestDto requestDto
    ) {
        LoginResult result = authService.reissue(requestDto.toCommand());
        LoginResponseDto response = LoginResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<LogoutResponseDto>> logout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        LogoutResult result = authService.logout(userId);
        LogoutResponseDto response = LogoutResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}