package com.naengpa.naengpamasterbackend.global.auth.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Value("${oauth2.frontend-redirect-url:http://localhost:3000}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        response.sendRedirect(buildFailureRedirectUrl(frontendRedirectUrl, exception));
    }

    public static String buildFailureRedirectUrl(String frontendRedirectUrl, Exception exception) {
        String code = resolveErrorCode(exception);
        String message = resolveMessage(code, exception);

        return UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("oauthError", code)
                .queryParam("oauthErrorMessage", message)
                .build()
                .toUriString();
    }

    private static String resolveErrorCode(Exception exception) {
        if (exception instanceof DisabledException) {
            return "inactive";
        }
        if (exception instanceof OAuth2AuthenticationException oauth2Exception
                && "access_denied".equals(oauth2Exception.getError().getErrorCode())) {
            return "cancelled";
        }
        String message = exception.getMessage() == null ? "" : exception.getMessage();
        if (message.contains("이미")) {
            return "duplicate";
        }
        if (message.contains("탈퇴") || message.contains("비활성")) {
            return "inactive";
        }
        return "failed";
    }

    private static String resolveMessage(String code, Exception exception) {
        return switch (code) {
            case "cancelled" -> "소셜 로그인이 취소되었습니다.";
            case "duplicate" -> exception.getMessage();
            case "inactive" -> exception.getMessage();
            default -> "소셜 로그인 중 오류가 발생했습니다. 다시 시도해주세요.";
        };
    }
}
