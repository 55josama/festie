package com.ojosama.userservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendPhoneVerificationRequestDto(

        @NotBlank(message = "휴대전화 번호는 필수입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "휴대전화 번호는 01012345678 형식이어야 합니다.")
        String phoneNumber
) {
}