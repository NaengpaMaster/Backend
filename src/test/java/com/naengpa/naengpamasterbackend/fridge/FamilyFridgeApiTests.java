package com.naengpa.naengpamasterbackend.fridge;

import com.naengpa.naengpamasterbackend.fridge.entity.Fridge;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInvite;
import com.naengpa.naengpamasterbackend.fridge.entity.FridgeInviteStatus;
import com.naengpa.naengpamasterbackend.fridge.repository.FridgeInviteRepository;
import com.naengpa.naengpamasterbackend.fridge.service.FridgeService;
import com.naengpa.naengpamasterbackend.global.security.JwtTokenProvider;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FamilyFridgeApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FridgeService fridgeService;

    @Autowired
    private FridgeInviteRepository fridgeInviteRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void premiumOwnerCanInviteAndInviteeCanAcceptFamilyFridge() throws Exception {
        Member owner = createMember("family-owner@example.com", "가족소유자");
        Member invitee = createMember("family-invitee@example.com", "가족초대자");
        Fridge ownerFridge = fridgeService.getOrCreateDefaultFridge(owner);
        grantPremium(owner, ownerFridge);
        String ownerToken = jwtTokenProvider.createAccessToken(owner.getEmail(), owner.getRole().name());
        String inviteeToken = jwtTokenProvider.createAccessToken(invitee.getEmail(), invitee.getRole().name());

        mockMvc.perform(post("/api/v1/fridges/me/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "family-invitee@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("family-invitee@example.com")));

        FridgeInvite invite = fridgeInviteRepository.findAllByInviteeMemberIdAndStatus(
                        invitee.getId(),
                        FridgeInviteStatus.PENDING
                )
                .stream()
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/api/v1/fridges/invites/{inviteId}/accept", invite.getFridgeInviteId())
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("family-invitee@example.com")));

        mockMvc.perform(get("/api/v1/fridges/accessible")
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("family-owner@example.com")));
    }

    @Test
    void freeOwnerCannotInviteFamilyMember() throws Exception {
        Member owner = createMember("family-free-owner@example.com", "무료소유자");
        createMember("family-free-invitee@example.com", "무료초대자");
        fridgeService.getOrCreateDefaultFridge(owner);
        String ownerToken = jwtTokenProvider.createAccessToken(owner.getEmail(), owner.getRole().name());

        mockMvc.perform(post("/api/v1/fridges/me/members")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "family-free-invitee@example.com"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("프리미엄 구독자만 가족 공유 냉장고를 관리할 수 있습니다")));
    }

    private Member createMember(String email, String nickname) {
        return memberRepository.save(Member.createUser(
                email,
                "password",
                nickname,
                HouseholdType.ONE_PERSON
        ));
    }

    private void grantPremium(Member member, Fridge fridge) {
        Long planId = jdbcTemplate.queryForObject(
                "SELECT subscription_plan_id FROM subscription_plans WHERE code = 'MONTHLY_PREMIUM'",
                Long.class
        );
        jdbcTemplate.update(
                """
                        INSERT INTO subscriptions (
                            member_id,
                            fridge_id,
                            subscription_plan_id,
                            status,
                            trial_started_at,
                            trial_ends_at,
                            current_period_start_at,
                            current_period_end_at,
                            next_billing_at,
                            created_at
                        ) VALUES (?, ?, ?, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '1 month', CURRENT_TIMESTAMP + INTERVAL '1 month', CURRENT_TIMESTAMP)
                        """,
                member.getId(),
                fridge.getFridgeId(),
                planId
        );
    }
}
