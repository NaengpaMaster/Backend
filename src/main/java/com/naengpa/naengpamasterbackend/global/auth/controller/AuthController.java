package com.naengpa.naengpamasterbackend.global.auth.controller;

import com.naengpa.naengpamasterbackend.global.auth.dto.LoginRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.LogoutRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.RefreshTokenRequest;
import com.naengpa.naengpamasterbackend.global.auth.dto.TokenResponse;
import com.naengpa.naengpamasterbackend.global.auth.service.AuthService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "이메일 로그인, 토큰 재발급, 로그아웃 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 accessToken과 refreshToken을 발급합니다. 탈퇴/비활성 회원은 로그인할 수 없습니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return buildTokenResponse("로그인에 성공했습니다.", authService.login(request));
    }

    @Operation(summary = "토큰 재발급", description = "refreshToken 쿠키 또는 요청 본문 refreshToken으로 새로운 accessToken/refreshToken을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
            @Parameter(description = "HttpOnly refreshToken 쿠키", hidden = true)
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = request == null || request.refreshToken() == null
                ? refreshTokenCookie
                : request.refreshToken();

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("리프레시 토큰이 없습니다."));
        }
        return buildTokenResponse("토큰이 재발급되었습니다.", authService.refreshToken(refreshToken));
    }

    @Operation(summary = "로그아웃", description = "refreshToken을 만료 처리하고 refreshToken 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(description = "HttpOnly refreshToken 쿠키", hidden = true)
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @RequestBody(required = false) LogoutRequest request
    ) {
        String refreshToken = request == null || request.refreshToken() == null ? refreshTokenCookie : request.refreshToken();
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createDeleteRefreshTokenCookie().toString())
                .body(ApiResponse.success("로그아웃되었습니다.", null));
    }

    private ResponseEntity<ApiResponse<TokenResponse>> buildTokenResponse(String message, TokenResponse tokenResponse) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(tokenResponse.refreshToken()).toString())
                .body(ApiResponse.success(message, tokenResponse));
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();
    }

    private ResponseCookie createDeleteRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }
}
