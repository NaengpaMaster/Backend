package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberRoleRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.global.auth.repository.RefreshTokenRepository;
import com.naengpa.naengpamasterbackend.global.exception.InvalidMemberRoleChangeException;
import com.naengpa.naengpamasterbackend.global.exception.InvalidMemberStatusChangeException;
import com.naengpa.naengpamasterbackend.global.exception.LastAdminDemotionException;
import com.naengpa.naengpamasterbackend.global.exception.MemberRoleAlreadyAppliedException;
import com.naengpa.naengpamasterbackend.global.exception.MemberStatusAlreadyAppliedException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AdminMemberServiceTest {

    private AdminMemberRepository adminMemberRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private AdminMemberService adminMemberService;

    @BeforeEach
    void setUp() {
        adminMemberRepository = mock(AdminMemberRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        adminMemberService = new AdminMemberService(adminMemberRepository, refreshTokenRepository);
    }

    @Test
    void updateMemberStatusDeactivatesAnotherMember() {
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

        verify(member).updateStatus(MemberStatus.INACTIVE);
        verify(refreshTokenRepository).findAllByMemberAndExpiredAtAfter(
                any(Member.class), any(LocalDateTime.class)
        );
    }

    @Test
    void updateMemberStatusActivatesMemberWithoutLookingUpRefreshTokens() {
        Member member = member(1L, MemberRole.USER, MemberStatus.INACTIVE);
        Member admin = member(2L, MemberRole.ADMIN, MemberStatus.ACTIVE);
        given(adminMemberRepository.findById(1L)).willReturn(Optional.of(member));
        given(adminMemberRepository.findByEmail("admin@example.com")).willReturn(Optional.of(admin));

        adminMemberService.updateMemberStatus(
                1L, new AdminMemberStatusRequest(MemberStatus.ACTIVE), "admin@example.com"
        );

        verify(member).updateStatus(MemberStatus.ACTIVE);
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
}
