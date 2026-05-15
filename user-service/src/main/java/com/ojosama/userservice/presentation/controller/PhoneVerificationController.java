package com.ojosama.userservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
import com.ojosama.userservice.application.service.PhoneVerificationService;
import com.ojosama.userservice.presentation.dto.request.SendPhoneVerificationRequestDto;
import com.ojosama.userservice.presentation.dto.request.VerifyPhoneVerificationRequestDto;
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
@RequestMapping("/v1/phone-verifications")
public class PhoneVerificationController {

    private final PhoneVerificationService phoneVerificationService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/send")
    public ApiResponse<Void> sendVerificationCode(
            @Valid @RequestBody SendPhoneVerificationRequestDto request
    ) {
        phoneVerificationService.sendVerificationCode(request.phoneNumber());

        return ApiResponse.success(null);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/verify")
    public ApiResponse<Void> verifyCode(
            @Valid @RequestBody VerifyPhoneVerificationRequestDto request
    ) {
        phoneVerificationService.verifyCode(request.phoneNumber(), request.code());

        return ApiResponse.success(null);
    }
}