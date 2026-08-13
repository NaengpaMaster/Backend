package com.naengpa.naengpamasterbackend.global.auth.oauth2.dto;

public record OAuth2SignupRequiredResponse(
        boolean emailRequired,
        String signupToken,
        String provider,
        String providerEmail
) {
}
