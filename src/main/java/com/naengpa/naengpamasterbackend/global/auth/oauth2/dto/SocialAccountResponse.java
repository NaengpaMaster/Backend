package com.naengpa.naengpamasterbackend.global.auth.oauth2.dto;

import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.SocialAccount;

import java.time.LocalDateTime;

public record SocialAccountResponse(
        Long socialAccountId,
        OAuth2Provider provider,
        String providerEmail,
        LocalDateTime createdAt
) {

    public static SocialAccountResponse from(SocialAccount socialAccount) {
        return new SocialAccountResponse(
                socialAccount.getSocialAccountId(),
                socialAccount.getProvider(),
                socialAccount.getProviderEmail(),
                socialAccount.getCreatedAt()
        );
    }
}
