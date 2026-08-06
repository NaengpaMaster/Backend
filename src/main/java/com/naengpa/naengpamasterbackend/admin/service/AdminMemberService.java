package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberRoleRequest;
import com.naengpa.naengpamasterbackend.admin.dto.request.AdminMemberStatusRequest;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberResponse;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.global.auth.entity.RefreshToken;
import com.naengpa.naengpamasterbackend.global.auth.repository.RefreshTokenRepository;
import com.naengpa.naengpamasterbackend.global.exception.InvalidMemberRoleChangeException;
import com.naengpa.naengpamasterbackend.global.exception.InvalidMemberStatusChangeException;
import com.naengpa.naengpamasterbackend.global.exception.LastAdminDemotionException;
import com.naengpa.naengpamasterbackend.global.exception.MemberNotFoundException;
import com.naengpa.naengpamasterbackend.global.exception.MemberRoleAlreadyAppliedException;
import com.naengpa.naengpamasterbackend.global.exception.MemberStatusAlreadyAppliedException;
import com.naengpa.naengpamasterbackend.member.entity.Member;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatusHistory;
import com.naengpa.naengpamasterbackend.member.repository.MemberStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private final AdminMemberRepository adminMemberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberStatusHistoryRepository memberStatusHistoryRepository;

    @Transactional(readOnly = true)
    public Page<AdminMemberResponse> getMembers(MemberRole role, MemberStatus status, String search, Pageable pageable) {
        return adminMemberRepository.findMembers(role, status, search, pageable)
                .map(AdminMemberResponse::from);
    }

    @Transactional
    public void updateMemberStatus(Long memberId, AdminMemberStatusRequest request, String adminEmail) {
        Member member = adminMemberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        MemberStatus previousStatus = member.getStatus();
        Member admin = adminMemberRepository.findByEmail(adminEmail)
                .orElseThrow(MemberNotFoundException::new);

        MemberStatus changedStatus = request.status();

        // 현재 상태와 동일한 상태로 변경하는 요청 방지
        if (member.getStatus() == changedStatus) {
            throw new MemberStatusAlreadyAppliedException();
        }

        // 관리자가 자기 자신을 비활성화하지 못하도록 방지
        if (member.getId().equals(admin.getId())
                && changedStatus == MemberStatus.INACTIVE) {
            throw new InvalidMemberStatusChangeException();
        }

        member.updateStatus(changedStatus);
        memberStatusHistoryRepository.save(
                MemberStatusHistory.create(memberId, previousStatus, changedStatus)
        );

        // 회원이 비활성화되면 로그인 상태도 해제
        if (changedStatus == MemberStatus.INACTIVE) {
            refreshTokenRepository.findAllByMemberAndExpiredAtAfter(member, LocalDateTime.now())
                    .forEach(RefreshToken::expireNow);
        }
    }

    @Transactional
    public void updateMemberRole(Long memberId, AdminMemberRoleRequest request, String adminEmail) {
        Member member = adminMemberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        Member admin = adminMemberRepository.findByEmail(adminEmail)
                .orElseThrow(MemberNotFoundException::new);

        MemberRole changedRole = request.role();

        // 이미 적용된 권한으로 변경하는 요청 방지
        if (member.getRole() == changedRole) {
            throw new MemberRoleAlreadyAppliedException();
        }

        // 관리자가 자기 자신의 관리자 권한을 해제하지 못하도록 방지
        if (member.getId().equals(admin.getId())
                && changedRole == MemberRole.USER) {
            throw new InvalidMemberRoleChangeException();
        }

        // 마지막 관리자의 권한 해제 방지
        if (member.getRole() == MemberRole.ADMIN
                && member.getStatus() == MemberStatus.ACTIVE
                && changedRole == MemberRole.USER
                && adminMemberRepository.countByRoleAndStatus(
                        MemberRole.ADMIN,
                        MemberStatus.ACTIVE
                ) <= 1) {
            throw new LastAdminDemotionException();
        }

        member.updateRole(changedRole);
    }

}
