package com.naengpa.naengpamasterbackend.global.auth.oauth2.controller;

import com.naengpa.naengpamasterbackend.global.auth.dto.TokenResponse;
import com.naengpa.naengpamasterbackend.global.auth.dto.EmailVerificationRequest;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.dto.OAuth2EmailCompletionRequest;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.service.OAuth2AccountService;
import com.naengpa.naengpamasterbackend.global.auth.service.EmailVerificationService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth/oauth2")
@RequiredArgsConstructor
public class OAuth2EmailCompletionController {

    private final OAuth2AccountService oauth2AccountService;
    private final EmailVerificationService emailVerificationService;


    @PostMapping("/email-verifications")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(
            @Valid @RequestBody EmailVerificationRequest request
    ) {
        emailVerificationService.sendOAuth2VerificationCode(request.email());
        return ResponseEntity.ok(ApiResponse.success("소셜 로그인 이메일 인증 코드가 발송되었습니다.", null));
    }

    @PostMapping("/email-completion")
    public ResponseEntity<ApiResponse<TokenResponse>> completeEmail(
            @Valid @RequestBody OAuth2EmailCompletionRequest request
    ) {
        TokenResponse tokenResponse = oauth2AccountService.completeEmail(
                request.signupToken(),
                request.email(),
                request.nickname(),
                request.householdType()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokenResponse.refreshToken()).toString())
                .body(ApiResponse.success("소셜 로그인 이메일 인증이 완료되었습니다.", tokenResponse));
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
    }
}
