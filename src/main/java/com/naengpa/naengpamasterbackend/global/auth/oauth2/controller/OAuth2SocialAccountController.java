package com.naengpa.naengpamasterbackend.global.auth.oauth2.controller;

import com.naengpa.naengpamasterbackend.global.auth.oauth2.dto.SocialAccountResponse;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.service.OAuth2AccountService;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
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
public class OAuth2SocialAccountController {

    private final OAuth2AccountService oauth2AccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SocialAccountResponse>>> getLinkedAccounts(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(oauth2AccountService.getLinkedAccounts(authentication.getName())));
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<ApiResponse<Void>> unlinkAccount(
            Authentication authentication,
            @PathVariable String provider
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
