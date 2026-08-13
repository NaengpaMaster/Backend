package com.naengpa.naengpamasterbackend.global.auth.oauth2.controller;

import com.naengpa.naengpamasterbackend.global.auth.oauth2.dto.SocialAccountResponse;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.service.OAuth2AccountService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/members/me/social-accounts")
@RequiredArgsConstructor
@Tag(name = "소셜 계정 연동", description = "회원의 카카오/네이버 소셜 계정 연동 조회 및 해지 API")
public class OAuth2SocialAccountController {

    private final OAuth2AccountService oauth2AccountService;

    @Operation(summary = "내 소셜 계정 연동 목록 조회", description = "로그인한 회원에게 연동된 카카오/네이버 소셜 계정 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SocialAccountResponse>>> getLinkedAccounts(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.success(oauth2AccountService.getLinkedAccounts(authentication.getName())));
    }

    @Operation(summary = "소셜 계정 연동 해지", description = "로그인한 회원의 특정 소셜 제공자 연동을 해지합니다. provider는 kakao 또는 naver를 사용합니다.")
    @DeleteMapping("/{provider}")
    public ResponseEntity<ApiResponse<Void>> unlinkAccount(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "소셜 제공자 코드: kakao, naver") @PathVariable String provider
    ) {
        oauth2AccountService.unlinkAccount(authentication.getName(), resolveProvider(provider));
        return ResponseEntity.ok(ApiResponse.success("소셜 연동이 해지되었습니다.", null));
    }

    private OAuth2Provider resolveProvider(String provider) {
        try {
            return OAuth2Provider.valueOf(provider.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인 제공자입니다.");
        }
    }
}
