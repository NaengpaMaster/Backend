package com.naengpa.naengpamasterbackend.global.auth.oauth2.repository;

import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2SignupToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuth2SignupTokenRepository extends JpaRepository<OAuth2SignupToken, Long> {

    Optional<OAuth2SignupToken> findByToken(String token);
}
