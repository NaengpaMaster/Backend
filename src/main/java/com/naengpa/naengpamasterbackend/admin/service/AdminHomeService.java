package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminHomeResponse;
import com.naengpa.naengpamasterbackend.admin.projection.RecipeCountProjection;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminProductRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminStatisticsRepository;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AdminHomeService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final AdminMemberRepository adminMemberRepository;
    private final AdminInquiryRepository adminInquiryRepository;
    private final AdminStatisticsRepository adminStatisticsRepository;
    private final AdminProductRepository adminProductRepository;

    // 관리자가 홈에서 확인할 오늘의 주요 운영 지표를 조회합니다.
    @Transactional(readOnly = true)
    public AdminHomeResponse getHome() {
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE);
        LocalDate today = now.toLocalDate();
        LocalDateTime startAt = today.atStartOfDay();
        LocalDateTime endExclusive = today.plusDays(1).atStartOfDay();
        RecipeCountProjection recipeCount = adminStatisticsRepository.countRecipesByCreatorType();

        return new AdminHomeResponse(
                adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER),
                adminMemberRepository.countByStatusAndRole(MemberStatus.INACTIVE, MemberRole.USER),
                adminMemberRepository.countNewMembers(startAt, endExclusive),
                adminMemberRepository.countInactiveMembers(startAt, endExclusive),
                adminInquiryRepository.countPendingInquiries(),
                adminInquiryRepository.countPendingInquiriesCreatedBefore(now.minusHours(24)),
                recipeCount.getTotalCount(),
                recipeCount.getMemberCount(),
                adminProductRepository.countByIsActiveTrue(),
                adminProductRepository.countByIsActiveFalse(),
                now
        );
    }
}
