package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminDashboardResponse;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final AdminMemberRepository adminMemberRepository;
    private final AdminInquiryRepository adminInquiryRepository;

    // 관리자·회원·미답변 문의 수로 구성된 기존 대시보드 요약을 조회합니다.
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        Long adminCount = adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.ADMIN);
        Long activeMembers = adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER);
        Long inactiveMembers = adminMemberRepository.countByStatusAndRole(MemberStatus.INACTIVE, MemberRole.USER);
        Long pendingInquiries = adminInquiryRepository.countPendingInquiries();
        return new AdminDashboardResponse(adminCount, activeMembers, inactiveMembers, pendingInquiries);
    }
}
