package com.naengpa.naengpamasterbackend.global.auth.oauth2.handler;

import com.naengpa.naengpamasterbackend.global.auth.dto.TokenResponse;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.dto.OAuth2SignupRequiredResponse;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.service.OAuth2AccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    public static final String LINK_ACCESS_TOKEN_SESSION_ATTRIBUTE = "OAUTH2_LINK_ACCESS_TOKEN";

    private final OAuth2AccountService oauth2AccountService;

    @Value("${oauth2.frontend-redirect-url:http://localhost:3000}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();

        try {
            OAuth2Provider provider = resolveProvider(oauth2Token.getAuthorizedClientRegistrationId());
            String providerUserId = resolveProviderUserId(provider, oauth2User.getAttributes());
            String providerEmail = resolveProviderEmail(provider, oauth2User.getAttributes());

            String linkAccessToken = resolveLinkAccessToken(request);
            if (linkAccessToken != null && !linkAccessToken.isBlank()) {
                TokenResponse tokenResponse = oauth2AccountService.linkAccount(
                        linkAccessToken,
                        provider,
                        providerUserId,
                        providerEmail
                );
                response.sendRedirect(buildTokenRedirectUrl(tokenResponse));
                return;
            }

            String redirectUrl = oauth2AccountService.loginLinkedAccount(provider, providerUserId)
                    .map(this::buildTokenRedirectUrl)
                    .orElseGet(() -> buildFirstLoginRedirectUrl(provider, providerUserId, providerEmail));

            response.sendRedirect(redirectUrl);
        } catch (RuntimeException exception) {
            response.sendRedirect(OAuth2LoginFailureHandler.buildFailureRedirectUrl(frontendRedirectUrl, exception));
        }
    }

    private String buildFirstLoginRedirectUrl(OAuth2Provider provider, String providerUserId, String providerEmail) {
        OAuth2SignupRequiredResponse signupRequired = oauth2AccountService.createEmailRequiredToken(
                provider,
                providerUserId,
                providerEmail
        );

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("oauthSignupToken", signupRequired.signupToken())
                .queryParam("provider", signupRequired.provider());

        if (signupRequired.providerEmail() != null && !signupRequired.providerEmail().isBlank()) {
            builder.queryParam("providerEmail", signupRequired.providerEmail());
        }

        return builder.build().toUriString();
    }

    private String buildTokenRedirectUrl(TokenResponse tokenResponse) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("oauthAccessToken", tokenResponse.accessToken())
                .queryParam("oauthRefreshToken", tokenResponse.refreshToken())
                .build()
                .toUriString();
    }

    private String resolveLinkAccessToken(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object value = request.getSession(false).getAttribute(LINK_ACCESS_TOKEN_SESSION_ATTRIBUTE);
        request.getSession(false).removeAttribute(LINK_ACCESS_TOKEN_SESSION_ATTRIBUTE);
        return value == null ? null : String.valueOf(value);
    }

    private OAuth2Provider resolveProvider(String registrationId) {
        return OAuth2Provider.valueOf(registrationId.trim().toUpperCase(Locale.ROOT));
    }

    private String resolveProviderUserId(OAuth2Provider provider, Map<String, Object> attributes) {
        if (provider == OAuth2Provider.KAKAO) {
            Object id = attributes.get("id");
            if (id == null) {
                throw new IllegalArgumentException("카카오 사용자 ID를 확인할 수 없습니다.");
            }
            return String.valueOf(id);
        }

        Map<String, Object> response = getNestedMap(attributes, "response");
        Object id = response.get("id");
        if (id == null) {
            throw new IllegalArgumentException("네이버 사용자 ID를 확인할 수 없습니다.");
        }
        return String.valueOf(id);
    }

    private String resolveProviderEmail(OAuth2Provider provider, Map<String, Object> attributes) {
        if (provider == OAuth2Provider.KAKAO) {
            Map<String, Object> kakaoAccount = getNestedMap(attributes, "kakao_account");
            Object email = kakaoAccount.get("email");
            return email == null ? null : String.valueOf(email);
        }

        Map<String, Object> response = getNestedMap(attributes, "response");
        Object email = response.get("email");
        return email == null ? null : String.valueOf(email);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNestedMap(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
