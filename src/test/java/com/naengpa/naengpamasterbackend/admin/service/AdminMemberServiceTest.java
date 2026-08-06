package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.global.auth.repository.RefreshTokenRepository;
import com.naengpa.naengpamasterbackend.member.entity.HouseholdType;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatusHistory;
import com.naengpa.naengpamasterbackend.member.repository.MemberStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminMemberServiceTest {

    private AdminMemberRepository adminMemberRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private MemberStatusHistoryRepository memberStatusHistoryRepository;
    private AdminMemberService adminMemberService;

    @BeforeEach
    void setUp() {
        adminMemberRepository = mock(AdminMemberRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        memberStatusHistoryRepository = mock(MemberStatusHistoryRepository.class);
        adminMemberService = new AdminMemberService(
                adminMemberRepository,
                refreshTokenRepository,
                memberStatusHistoryRepository
        );
    }

    @Test
    void updateMemberStatusSavesHistory() {
        Member member = Member.createUser(
                "member@example.com",
                "encoded-password",
                "회원",
                HouseholdType.ETC
        );
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(refreshTokenRepository.findAllByMemberAndExpiredAtAfter(
                org.mockito.ArgumentMatchers.eq(member),
                org.mockito.ArgumentMatchers.any()
        )).willReturn(java.util.List.of());

        adminMemberService.updateMemberStatus(
                1L,
                new AdminMemberStatusRequest(MemberStatus.INACTIVE)
        );

        ArgumentCaptor<MemberStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(MemberStatusHistory.class);
        verify(memberStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(member.getStatus()).isEqualTo(MemberStatus.INACTIVE);
        assertThat(historyCaptor.getValue().getMemberId()).isEqualTo(1L);
        assertThat(historyCaptor.getValue().getPreviousStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(historyCaptor.getValue().getChangedStatus()).isEqualTo(MemberStatus.INACTIVE);
    }

    @Test
    void updateMemberStatusDoesNotSaveHistoryWhenStatusIsUnchanged() {
        Member member = Member.createUser(
                "member@example.com",
                "encoded-password",
                "회원",
                HouseholdType.ETC
        );
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));

        adminMemberService.updateMemberStatus(
                1L,
                new AdminMemberStatusRequest(MemberStatus.ACTIVE)
        );

        verify(memberStatusHistoryRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(refreshTokenRepository, never())
                .findAllByMemberAndExpiredAtAfter(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                );
    }

    @Test
    void updateMemberStatusSavesReactivationHistory() {
        Member member = Member.createUser(
                "member@example.com",
                "encoded-password",
                "회원",
                HouseholdType.ETC
        );
        member.updateStatus(MemberStatus.INACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));

        adminMemberService.updateMemberStatus(
                1L,
                new AdminMemberStatusRequest(MemberStatus.ACTIVE)
        );

        ArgumentCaptor<MemberStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(MemberStatusHistory.class);
        verify(memberStatusHistoryRepository).save(historyCaptor.capture());
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(historyCaptor.getValue().getPreviousStatus()).isEqualTo(MemberStatus.INACTIVE);
        assertThat(historyCaptor.getValue().getChangedStatus()).isEqualTo(MemberStatus.ACTIVE);
    }
}
