package com.naengpa.naengpamasterbackend.score.service;

import com.naengpa.naengpamasterbackend.global.auth.dto.MemberResponse;
import com.naengpa.naengpamasterbackend.global.auth.dto.SignUpRequest;
import com.naengpa.naengpamasterbackend.global.auth.entity.EmailVerification;
import com.naengpa.naengpamasterbackend.global.auth.repository.EmailVerificationRepository;
import com.naengpa.naengpamasterbackend.global.auth.service.AuthService;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.score.entity.Score;
import com.naengpa.naengpamasterbackend.score.entity.ScoreHistory;
import com.naengpa.naengpamasterbackend.score.entity.ScoreReason;
import com.naengpa.naengpamasterbackend.score.repository.ScoreHistoryRepository;
import com.naengpa.naengpamasterbackend.score.repository.ScoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthSignupScoreIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private ScoreHistoryRepository scoreHistoryRepository;

    @Test
    @DisplayName("회원가입 시 10점으로 초기화되고 SIGNUP_BONUS 이력이 생긴다")
    void 회원가입시_10점_초기화되고_SIGNUP_BONUS_이력이_생긴다() {
        String email = "test-signup@example.com";

        EmailVerification verification = EmailVerification.create(email, "000000", LocalDateTime.now().plusMinutes(10));
        verification.verify(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        SignUpRequest request = new SignUpRequest(
                email, "abcd1234!", "abcd1234!", "", HouseholdType.ONE_PERSON
        );

        MemberResponse response = authService.signup(request);

        Score score = scoreRepository.findByMemberId(response.memberId()).orElseThrow();
        assertThat(score.getScore()).isEqualTo(10);

        List<ScoreHistory> histories = scoreHistoryRepository
                .findByMemberIdOrderByCreatedAtDesc(response.memberId(), Pageable.unpaged())
                .getContent();
        assertThat(histories)
                .extracting(ScoreHistory::getScoreReason)
                .contains(ScoreReason.SIGNUP_BONUS);
        assertThat(histories)
                .filteredOn(h -> h.getScoreReason() == ScoreReason.SIGNUP_BONUS)
                .extracting(ScoreHistory::getScoreDelta)
                .containsExactly(10);
    }
}