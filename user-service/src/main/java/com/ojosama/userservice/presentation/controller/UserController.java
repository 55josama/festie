package com.ojosama.userservice.presentation.controller;

import com.ojosama.common.response.ApiResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
@Tag(name = "User", description = "회원 API")
public class UserController {


    private final UserService userService;

    // 회원가입
    @PostMapping
    @Operation(
            summary = "회원가입",
            description = "새로운 회원을 생성합니다."
    )

    public ResponseEntity<ApiResponse<CreateUserResponseDto>> createUser(
            @Valid @RequestBody CreateUserRequestDto request
    ) {
        CreateUserResult result = userService.createUser(request.toCommand());
        CreateUserResponseDto response = CreateUserResponseDto.from(result);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }

    // 내 정보 조회
    @GetMapping("/me")
    @Operation(
            summary = "내 정보 조회",
            description = "로그인한 회원의 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<GetUserResponseDto>> getMyInfo(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        GetUserResult result = userService.getUser(new GetUserQuery(userId));
        GetUserResponseDto response = GetUserResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 내 정보 수정
    @PatchMapping("/me")
    @Operation(
            summary = "내 정보 수정",
            description = "로그인한 회원의 정보를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )

    public ResponseEntity<ApiResponse<UpdateUserResponseDto>> updateMyInfo(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequestDto request
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        UpdateUserResult result = userService.updateUser(request.toCommand(userId));
        UpdateUserResponseDto response = UpdateUserResponseDto.from(result);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    @Operation(
            summary = "회원 탈퇴",
            description = "로그인한 회원을 탈퇴 처리합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteMyInfo(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        userService.deleteUser(new DeleteUserCommand(userId));

        return ResponseEntity.ok(ApiResponse.deleted());
    }
}