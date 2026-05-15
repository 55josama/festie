package com.ojosama.userservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyPhoneVerificationRequestDto(

        @NotBlank(message = "휴대전화 번호는 필수입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "휴대전화 번호는 01012345678 형식이어야 합니다.")
        String phoneNumber,

        @NotBlank(message = "인증번호는 필수입니다.")
        @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        String code
) {
}