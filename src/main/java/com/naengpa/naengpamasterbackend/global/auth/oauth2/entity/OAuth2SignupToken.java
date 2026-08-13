package com.naengpa.naengpamasterbackend.global.auth.oauth2.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth2_signup_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuth2SignupToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "oauth2_signup_token_id")
    private Long oauth2SignupTokenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OAuth2Provider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "provider_email")
    private String providerEmail;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static OAuth2SignupToken create(
            OAuth2Provider provider,
            String providerUserId,
            String providerEmail,
            String token,
            LocalDateTime expiredAt
    ) {
        OAuth2SignupToken signupToken = new OAuth2SignupToken();
        signupToken.provider = provider;
        signupToken.providerUserId = providerUserId;
        signupToken.providerEmail = providerEmail;
        signupToken.token = token;
        signupToken.expiredAt = expiredAt;
        return signupToken;
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiredAt.isAfter(now);
    }

    public boolean isCompleted() {
        return completedAt != null;
    }

    public void complete() {
        completedAt = LocalDateTime.now();
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
