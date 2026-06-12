package com.ojosama.userservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.userservice.application.dto.result.LoginResult;
import com.ojosama.userservice.application.dto.result.LogoutResult;
import com.ojosama.userservice.application.service.AuthService;
import com.ojosama.userservice.application.service.EmailVerificationService;
import com.ojosama.userservice.presentation.dto.request.LoginRequestDto;
import com.ojosama.userservice.presentation.dto.request.ReissueTokenRequestDto;
import com.ojosama.userservice.presentation.dto.request.SendEmailVerificationCodeRequestDto;
import com.ojosama.userservice.presentation.dto.request.VerifyEmailCodeRequestDto;
import com.ojosama.userservice.presentation.dto.response.EmailVerificationResponseDto;
import com.ojosama.userservice.presentation.dto.response.LoginResponseDto;
import com.ojosama.userservice.presentation.dto.response.LogoutResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/login")
    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하고 Access Token, Refresh Token을 발급합니다."
    )
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto
    ) {
        LoginResult result = authService.login(requestDto.toCommand());
        LoginResponseDto response = LoginResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/reissue")
    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token을 검증하고 새로운 Access Token, Refresh Token을 발급합니다."
    )
    public ResponseEntity<ApiResponse<LoginResponseDto>> reissue(
            @Valid @RequestBody ReissueTokenRequestDto requestDto
    ) {
        LoginResult result = authService.reissue(requestDto.toCommand());
        LoginResponseDto response = LoginResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "로그인한 사용자의 Refresh Token을 만료 처리합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<LogoutResponseDto>> logout(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        LogoutResult result = authService.logout(userId);
        LogoutResponseDto response = LogoutResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/email/send-code")
    @Operation(
            summary = "이메일 인증번호 발송",
            description = "회원가입에 사용할 이메일로 6자리 인증번호를 발송합니다."
    )
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> sendEmailVerificationCode(
            @Valid @RequestBody SendEmailVerificationCodeRequestDto requestDto
    ) {
        emailVerificationService.sendCode(requestDto.email());

        EmailVerificationResponseDto response = new EmailVerificationResponseDto(
                requestDto.email(),
                "이메일 인증번호를 발송했습니다."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/email/verify-code")
    @Operation(
            summary = "이메일 인증번호 검증",
            description = "이메일로 발송된 6자리 인증번호를 검증합니다."
    )
    public ResponseEntity<ApiResponse<EmailVerificationResponseDto>> verifyEmailCode(
            @Valid @RequestBody VerifyEmailCodeRequestDto requestDto
    ) {
        emailVerificationService.verifyCode(requestDto.email(), requestDto.code());

        EmailVerificationResponseDto response = new EmailVerificationResponseDto(
                requestDto.email(),
                "이메일 인증이 완료되었습니다."
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
