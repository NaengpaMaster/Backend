package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberRoleRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberDetailResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatusHistoryResponse;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.global.auth.repository.RefreshTokenRepository;
import com.naengpa.naengpamasterbackend.global.exception.InvalidMemberRoleChangeException;
import com.naengpa.naengpamasterbackend.global.exception.InvalidMemberStatusChangeException;
import com.naengpa.naengpamasterbackend.global.exception.LastAdminDemotionException;
import com.naengpa.naengpamasterbackend.global.exception.MemberRoleAlreadyAppliedException;
import com.naengpa.naengpamasterbackend.global.exception.MemberStatusAlreadyAppliedException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatusHistory;
import com.naengpa.naengpamasterbackend.member.repository.MemberStatusHistoryRepository;
import com.naengpa.naengpamasterbackend.score.entity.Score;
import com.naengpa.naengpamasterbackend.score.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminMemberServiceTest {

    private AdminMemberRepository adminMemberRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private MemberStatusHistoryRepository memberStatusHistoryRepository;
    private ScoreRepository scoreRepository;
    private AdminMemberService adminMemberService;

    @BeforeEach
    void setUp() {
        adminMemberRepository = mock(AdminMemberRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        memberStatusHistoryRepository = mock(MemberStatusHistoryRepository.class);
        scoreRepository = mock(ScoreRepository.class);
        adminMemberService = new AdminMemberService(
                adminMemberRepository,
                refreshTokenRepository,
                memberStatusHistoryRepository,
                scoreRepository
        );
    }

    @Test
    void getMemberDetailReturnsActiveMemberAndScore() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 1, 10, 0);
        Member member = memberDetail(1L, MemberRole.USER, MemberStatus.ACTIVE, createdAt);
        Score score = mock(Score.class);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(scoreRepository.findByMemberId(1L)).willReturn(Optional.of(score));
        given(score.getScore()).willReturn(72);

        AdminMemberDetailResponse response = adminMemberService.getMemberDetail(1L);

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("member@example.com");
        assertThat(response.nickname()).isEqualTo("회원");
        assertThat(response.householdType()).isEqualTo(HouseholdType.ONE_PERSON);
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.status()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(response.role()).isEqualTo(MemberRole.USER);
        assertThat(response.naengpaScore()).isEqualTo(72);
    }

    @Test
    void getMemberDetailReturnsInactiveMemberWithoutScore() {
        Member member = memberDetail(
                1L, MemberRole.USER, MemberStatus.INACTIVE,
                LocalDateTime.of(2026, 8, 1, 10, 0)
        );
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(scoreRepository.findByMemberId(1L)).willReturn(Optional.empty());

        AdminMemberDetailResponse response = adminMemberService.getMemberDetail(1L);

        assertThat(response.status()).isEqualTo(MemberStatus.INACTIVE);
        assertThat(response.naengpaScore()).isNull();
    }

    @Test
    void getMemberDetailRejectsUnknownMember() {
        given(adminMemberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminMemberService.getMemberDetail(999L))
                .isInstanceOf(MemberNotFoundException.class);

        verify(scoreRepository, never()).findByMemberId(any());
    }

    @Test
    void getMemberStatusHistoriesReturnsRequestedPeriodPage() {
        StatisticsPeriod period = StatisticsPeriod.of(
                java.time.LocalDate.of(2026, 8, 1),
                java.time.LocalDate.of(2026, 8, 3)
        );
        PageRequest pageable = PageRequest.of(0, 10);
        AdminMemberStatusHistoryResponse history = new AdminMemberStatusHistoryResponse(
                10L,
                1L,
                "회원",
                "member@example.com",
                MemberStatus.ACTIVE,
                MemberStatus.INACTIVE,
                MemberStatus.INACTIVE,
                LocalDateTime.of(2026, 8, 2, 15, 0)
        );
        Page<AdminMemberStatusHistoryResponse> expected = new PageImpl<>(List.of(history), pageable, 1);
        given(memberStatusHistoryRepository.findAdminStatusHistories(
                period.startAt(), period.endExclusive(), pageable
        )).willReturn(expected);

        Page<AdminMemberStatusHistoryResponse> response =
                adminMemberService.getMemberStatusHistories(period, pageable);

        assertThat(response).isSameAs(expected);
        verify(memberStatusHistoryRepository).findAdminStatusHistories(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 4, 0, 0),
                pageable
        );
    }

    @Test
    void updateMemberStatusDeactivatesAnotherMemberAndSavesHistory() {
        Member member = member(1L, MemberRole.USER, MemberStatus.ACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));
        given(refreshTokenRepository.findAllByMemberAndExpiredAtAfter(
                any(Member.class), any(LocalDateTime.class)
        )).willReturn(List.of());

        adminMemberService.updateMemberStatus(
                1L, new AdminMemberStatusRequest(MemberStatus.INACTIVE), "admin@example.com"
        );

        ArgumentCaptor<MemberStatusHistory> captor = ArgumentCaptor.forClass(MemberStatusHistory.class);
        verify(member).updateStatus(MemberStatus.INACTIVE);
        verify(memberStatusHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
        assertThat(captor.getValue().getPreviousStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(captor.getValue().getChangedStatus()).isEqualTo(MemberStatus.INACTIVE);
        verify(refreshTokenRepository).findAllByMemberAndExpiredAtAfter(
                any(Member.class), any(LocalDateTime.class)
        );
    }

    @Test
    void updateMemberStatusActivatesMemberAndSavesHistoryWithoutLookingUpRefreshTokens() {
        Member member = member(1L, MemberRole.USER, MemberStatus.INACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        adminMemberService.updateMemberStatus(
                1L, new AdminMemberStatusRequest(MemberStatus.ACTIVE), "admin@example.com"
        );

        verify(member).updateStatus(MemberStatus.ACTIVE);
        verify(memberStatusHistoryRepository).save(any(MemberStatusHistory.class));
        verify(refreshTokenRepository, never()).findAllByMemberAndExpiredAtAfter(any(), any());
    }

    @Test
    void updateMemberStatusRejectsAlreadyAppliedStatus() {
        Member member = member(1L, MemberRole.USER, MemberStatus.ACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminMemberService.updateMemberStatus(
                1L, new AdminMemberStatusRequest(MemberStatus.ACTIVE), "admin@example.com"
        )).isInstanceOf(MemberStatusAlreadyAppliedException.class);

        verify(member, never()).updateStatus(any());
        verify(memberStatusHistoryRepository, never()).save(any());
    }

    @Test
    void updateMemberStatusRejectsSelfDeactivation() {
        Member admin = member(1L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(admin));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminMemberService.updateMemberStatus(
                1L, new AdminMemberStatusRequest(MemberStatus.INACTIVE), "admin@example.com"
        )).isInstanceOf(InvalidMemberStatusChangeException.class);

        verify(admin, never()).updateStatus(any());
        verify(memberStatusHistoryRepository, never()).save(any());
    }

    @Test
    void updateMemberRolePromotesUserToAdmin() {
        Member member = member(1L, MemberRole.USER, MemberStatus.ACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        adminMemberService.updateMemberRole(
                1L, new AdminMemberRoleRequest(MemberRole.ADMIN), "admin@example.com"
        );

        verify(member).updateRole(MemberRole.ADMIN);
        verify(adminMemberRepository, never()).countByRoleAndStatus(any(), any());
    }

    @Test
    void updateMemberRoleRejectsAlreadyAppliedRole() {
        Member member = member(1L, MemberRole.USER, MemberStatus.ACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminMemberService.updateMemberRole(
                1L, new AdminMemberRoleRequest(MemberRole.USER), "admin@example.com"
        )).isInstanceOf(MemberRoleAlreadyAppliedException.class);

        verify(member, never()).updateRole(any());
    }

    @Test
    void updateMemberRoleRejectsSelfDemotion() {
        Member admin = member(1L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(admin));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminMemberService.updateMemberRole(
                1L, new AdminMemberRoleRequest(MemberRole.USER), "admin@example.com"
        )).isInstanceOf(InvalidMemberRoleChangeException.class);

        verify(admin, never()).updateRole(any());
    }

    @Test
    void updateMemberRoleRejectsLastActiveAdminDemotion() {
        Member member = member(1L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));
        given(adminMemberRepository.countByRoleAndStatus(MemberRole.ADMIN, MemberStatus.ACTIVE))
                .willReturn(1);

        assertThatThrownBy(() -> adminMemberService.updateMemberRole(
                1L, new AdminMemberRoleRequest(MemberRole.USER), "admin@example.com"
        )).isInstanceOf(LastAdminDemotionException.class);

        verify(member, never()).updateRole(any());
    }

    @Test
    void updateMemberRoleAllowsInactiveAdminDemotion() {
        Member member = member(1L, MemberRole.ADMIN, MemberStatus.INACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        adminMemberService.updateMemberRole(
                1L, new AdminMemberRoleRequest(MemberRole.USER), "admin@example.com"
        );

        verify(member).updateRole(MemberRole.USER);
        verify(adminMemberRepository, never()).countByRoleAndStatus(any(), any());
    }

    private Member member(Long id, MemberRole role, MemberStatus status) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(id);
        given(member.getRole()).willReturn(role);
        given(member.getStatus()).willReturn(status);
        return member;
    }

    private Member memberDetail(
            Long id,
            MemberRole role,
            MemberStatus status,
            LocalDateTime createdAt
    ) {
        Member member = member(id, role, status);
        given(member.getEmail()).willReturn("member@example.com");
        given(member.getNickname()).willReturn("회원");
        given(member.getHouseholdType()).willReturn(HouseholdType.ONE_PERSON);
        given(member.getCreatedAt()).willReturn(createdAt);
        return member;
    }
}
