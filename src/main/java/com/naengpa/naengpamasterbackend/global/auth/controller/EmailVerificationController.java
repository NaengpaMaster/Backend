package com.naengpa.naengpamasterbackend.global.auth.controller;

import com.naengpa.naengpamasterbackend.global.auth.dto.EmailVerificationConfirmRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.EmailVerificationRequest;
import com.naengpa.naengpamasterbackend.global.auth.service.EmailVerificationService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/email-verifications")
@Tag(name = "이메일 인증", description = "회원가입 및 OAuth2 추가 정보 입력용 이메일 인증 API")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입에 사용할 이메일로 인증 코드를 발송합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        emailVerificationService.sendVerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.success("인증 코드가 발송되었습니다.", null));
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "이메일로 발송된 인증 코드를 검증해 인증 완료 상태로 처리합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmVerificationCode(
            @Valid @RequestBody EmailVerificationConfirmRequest request
    ) {
        emailVerificationService.confirmVerificationCode(request.email(), request.code());
        return ResponseEntity.ok(ApiResponse.success("이메일 인증이 완료되었습니다.", null));
    }
}
