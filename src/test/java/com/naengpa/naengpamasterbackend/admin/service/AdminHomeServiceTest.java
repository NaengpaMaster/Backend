package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminHomeResponse;
import com.naengpa.naengpamasterbackend.admin.projection.RecipeCountProjection;
import com.naengpa.naengpamasterbackend.admin.repository.AdminInquiryRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminProductRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminStatisticsRepository;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AdminHomeServiceTest {

    private AdminMemberRepository adminMemberRepository;
    private AdminInquiryRepository adminInquiryRepository;
    private AdminStatisticsRepository adminStatisticsRepository;
    private AdminProductRepository adminProductRepository;
    private AdminHomeService adminHomeService;

    @BeforeEach
    void setUp() {
        adminMemberRepository = mock(AdminMemberRepository.class);
        adminInquiryRepository = mock(AdminInquiryRepository.class);
        adminStatisticsRepository = mock(AdminStatisticsRepository.class);
        adminProductRepository = mock(AdminProductRepository.class);
        adminHomeService = new AdminHomeService(
                adminMemberRepository,
                adminInquiryRepository,
                adminStatisticsRepository,
                adminProductRepository
        );
    }

    @Test
    void getHomeReturnsCurrentOperationalSummary() {
        RecipeCountProjection recipeCount = mock(RecipeCountProjection.class);
        given(adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER))
                .willReturn(100L);
        given(adminMemberRepository.countByStatusAndRole(MemberStatus.INACTIVE, MemberRole.USER))
                .willReturn(5L);
        given(adminMemberRepository.countNewMembers(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(3L);
        given(adminMemberRepository.countInactiveMembers(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(1L);
        given(adminInquiryRepository.countPendingInquiries()).willReturn(4L);
        given(adminInquiryRepository.countPendingInquiriesCreatedBefore(any(LocalDateTime.class)))
                .willReturn(2L);
        given(adminStatisticsRepository.countRecipesByCreatorType()).willReturn(recipeCount);
        given(recipeCount.getTotalCount()).willReturn(50L);
        given(recipeCount.getMemberCount()).willReturn(12L);
        given(adminProductRepository.countByIsActiveTrue()).willReturn(80L);
        given(adminProductRepository.countByIsActiveFalse()).willReturn(8L);

        AdminHomeResponse response = adminHomeService.getHome();

        assertThat(response.activeMemberCount()).isEqualTo(100L);
        assertThat(response.inactiveMemberCount()).isEqualTo(5L);
        assertThat(response.todayNewMemberCount()).isEqualTo(3L);
        assertThat(response.todayInactiveMemberCount()).isEqualTo(1L);
        assertThat(response.pendingInquiryCount()).isEqualTo(4L);
        assertThat(response.overduePendingInquiryCount()).isEqualTo(2L);
        assertThat(response.totalRecipeCount()).isEqualTo(50L);
        assertThat(response.memberRecipeCount()).isEqualTo(12L);
        assertThat(response.activeProductCount()).isEqualTo(80L);
        assertThat(response.inactiveProductCount()).isEqualTo(8L);
        assertThat(response.refreshedAt()).isNotNull();
    }
}
