package com.naengpa.naengpamasterbackend.global.auth.oauth2;

import com.naengpa.naengpamasterbackend.global.auth.entity.EmailVerification;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2Provider;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.OAuth2SignupToken;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.entity.SocialAccount;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.repository.OAuth2SignupTokenRepository;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.repository.SocialAccountRepository;
import com.naengpa.naengpamasterbackend.global.auth.oauth2.service.OAuth2AccountService;
import com.naengpa.naengpamasterbackend.global.auth.repository.EmailVerificationRepository;
import com.naengpa.naengpamasterbackend.global.exception.WithdrawnEmailException;
import com.naengpa.naengpamasterbackend.global.security.JwtTokenProvider;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.DisabledException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OAuth2SocialAccountApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private OAuth2SignupTokenRepository oauth2SignupTokenRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private OAuth2AccountService oauth2AccountService;

    @Test
    void getSocialAccounts_returnsLinkedProviders() throws Exception {
        Member member = createMember("oauth-linked-list@example.com", "소셜목록회원");
        socialAccountRepository.save(SocialAccount.create(member.getId(), OAuth2Provider.KAKAO, "kakao-list-id", "kakao@example.com"));
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());

        mockMvc.perform(get("/api/v1/members/me/social-accounts")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("KAKAO")))
                .andExpect(content().string(containsString("kakao@example.com")));
    }

    @Test
    void unlinkSocialAccount_removesProviderWhenAnotherProviderRemains() throws Exception {
        Member member = createMember("oauth-unlink@example.com", "소셜해지회원");
        socialAccountRepository.save(SocialAccount.create(member.getId(), OAuth2Provider.KAKAO, "kakao-unlink-id", "kakao@example.com"));
        socialAccountRepository.save(SocialAccount.create(member.getId(), OAuth2Provider.NAVER, "naver-unlink-id", "naver@example.com"));
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());

        mockMvc.perform(delete("/api/v1/members/me/social-accounts/KAKAO")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/members/me/social-accounts")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("KAKAO"))))
                .andExpect(content().string(containsString("NAVER")));
    }

    @Test
    void unlinkSocialAccount_blocksLastLinkedProvider() throws Exception {
        Member member = createMember("oauth-last-unlink@example.com", "마지막해지회원");
        socialAccountRepository.save(SocialAccount.create(member.getId(), OAuth2Provider.KAKAO, "kakao-last-id", "kakao@example.com"));
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());

        mockMvc.perform(delete("/api/v1/members/me/social-accounts/KAKAO")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("마지막 소셜 연동은 해지할 수 없습니다")));
    }

    @Test
    void loginLinkedAccount_blocksInactiveMember() {
        Member member = createMember("oauth-inactive-linked@example.com", "비활성연동회원");
        member.updateStatus(MemberStatus.INACTIVE);
        memberRepository.saveAndFlush(member);
        socialAccountRepository.save(SocialAccount.create(member.getId(), OAuth2Provider.KAKAO, "kakao-inactive-id", "kakao@example.com"));

        assertThatThrownBy(() -> oauth2AccountService.loginLinkedAccount(OAuth2Provider.KAKAO, "kakao-inactive-id"))
                .isInstanceOf(DisabledException.class)
                .hasMessageContaining("탈퇴 처리된 회원입니다");
    }

    @Test
    void loginOrCreateWithVerifiedEmail_blocksInactiveEmailMember() {
        Member member = createMember("oauth-inactive-email@example.com", "비활성이메일회원");
        member.updateStatus(MemberStatus.INACTIVE);
        memberRepository.saveAndFlush(member);

        assertThatThrownBy(() -> oauth2AccountService.loginOrCreateWithVerifiedEmail(
                OAuth2Provider.NAVER,
                "naver-inactive-email-id",
                member.getEmail()
        ))
                .isInstanceOf(WithdrawnEmailException.class)
                .hasMessageContaining("가입 이력이 있는 이메일입니다");
    }

    @Test
    void completeEmail_createsMemberWithProfileFields() {
        String email = "oauth-profile-complete@example.com";
        OAuth2SignupToken signupToken = oauth2SignupTokenRepository.save(OAuth2SignupToken.create(
                OAuth2Provider.KAKAO,
                "kakao-profile-id",
                null,
                "oauth-profile-token",
                LocalDateTime.now().plusMinutes(15)
        ));
        EmailVerification emailVerification = EmailVerification.create(
                email,
                "123456",
                LocalDateTime.now().plusMinutes(10)
        );
        emailVerification.verify(LocalDateTime.now());
        emailVerificationRepository.save(emailVerification);

        oauth2AccountService.completeEmail(
                signupToken.getToken(),
                email,
                "소셜프로필회원",
                HouseholdType.TWO_PERSON
        );

        Member member = memberRepository.findByEmail(email).orElseThrow();
        assertThat(member.getNickname()).isEqualTo("소셜프로필회원");
        assertThat(member.getHouseholdType()).isEqualTo(HouseholdType.TWO_PERSON);
    }

    private Member createMember(String email, String nickname) {
        return memberRepository.save(Member.createUser(
                email,
                "password",
                nickname,
                HouseholdType.ONE_PERSON
        ));
    }
}
