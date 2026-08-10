package com.naengpa.naengpamasterbackend.global.auth.oauth2.repository;

import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(OAuth2Provider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(OAuth2Provider provider, String providerUserId);

    List<SocialAccount> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    Optional<SocialAccount> findByMemberIdAndProvider(Long memberId, OAuth2Provider provider);

    long countByMemberId(Long memberId);
}
