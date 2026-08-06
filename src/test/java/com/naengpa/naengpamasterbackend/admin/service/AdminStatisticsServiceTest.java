package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberUsageStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.projection.DailyCountProjection;
import com.naengpa.naengpamasterbackend.admin.projection.DailyServiceUsageProjection;
import com.naengpa.naengpamasterbackend.admin.projection.ServiceUsageCountProjection;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminScoreRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminStatisticsRepository;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AdminStatisticsServiceTest {

    private AdminMemberRepository adminMemberRepository;
    private AdminStatisticsRepository adminStatisticsRepository;
    private AdminStatisticsService adminStatisticsService;

    @BeforeEach
    void setUp() {
        adminMemberRepository = mock(AdminMemberRepository.class);
        adminStatisticsRepository = mock(AdminStatisticsRepository.class);
        adminStatisticsService = new AdminStatisticsService(
                adminStatisticsRepository,
                mock(AdminScoreRepository.class),
                adminMemberRepository
        );
    }

    @Test
    void getMemberUsageStatisticsReturnsUsageRatesAndFillsMissingDatesWithZero() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3)
        );
        ServiceUsageCountProjection fridgeTotal = serviceCount("fridge", 4L);
        ServiceUsageCountProjection shoppingTotal = serviceCount("shopping", 2L);
        ServiceUsageCountProjection recipeTotal = serviceCount("recipe", 1L);
        DailyServiceUsageProjection fridgeFirst =
                dailyServiceCount("fridge", LocalDate.of(2026, 8, 1), 3L);
        DailyServiceUsageProjection fridgeThird =
                dailyServiceCount("fridge", LocalDate.of(2026, 8, 3), 2L);
        DailyServiceUsageProjection shoppingSecond =
                dailyServiceCount("shopping", LocalDate.of(2026, 8, 2), 2L);
        given(adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER))
                .willReturn(10L);
        given(adminStatisticsRepository.countServiceUsageMembers(period.startAt(), period.endExclusive()))
                .willReturn(List.of(fridgeTotal, shoppingTotal, recipeTotal));
        given(adminStatisticsRepository.countDailyServiceUsageMembers(period.startAt(), period.endExclusive()))
                .willReturn(List.of(fridgeFirst, fridgeThird, shoppingSecond));

        AdminMemberUsageStatisticsResponse response =
                adminStatisticsService.getMemberUsageStatistics(period);

        assertThat(response.activeMemberCount()).isEqualTo(10L);
        assertThat(response.fridge().userCount()).isEqualTo(4L);
        assertThat(response.fridge().usageRate()).isEqualTo(40.0);
        assertThat(response.fridge().dailyStatistics()).containsExactly(
                new AdminMemberUsageStatisticsResponse.DailyUsage(LocalDate.of(2026, 8, 1), 3L),
                new AdminMemberUsageStatisticsResponse.DailyUsage(LocalDate.of(2026, 8, 2), 0L),
                new AdminMemberUsageStatisticsResponse.DailyUsage(LocalDate.of(2026, 8, 3), 2L)
        );
        assertThat(response.shopping().usageRate()).isEqualTo(20.0);
        assertThat(response.recipe().usageRate()).isEqualTo(10.0);
        assertThat(response.recipe().dailyStatistics())
                .allMatch(statistic -> statistic.userCount() == 0L);
    }

    @Test
    void getMemberStatisticsFillsMissingDatesWithZero() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3)
        );
        LocalDateTime startAt = period.startAt();
        LocalDateTime endExclusive = period.endExclusive();
        DailyCountProjection augustFirstNewMembers = dailyCount(LocalDate.of(2026, 8, 1), 2L);
        DailyCountProjection augustThirdNewMembers = dailyCount(LocalDate.of(2026, 8, 3), 1L);
        DailyCountProjection augustSecondInactiveMembers = dailyCount(LocalDate.of(2026, 8, 2), 1L);

        given(adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER))
                .willReturn(100L);
        given(adminMemberRepository.countByStatusAndRole(MemberStatus.INACTIVE, MemberRole.USER))
                .willReturn(5L);
        given(adminMemberRepository.countDailyNewMembers(startAt, endExclusive))
                .willReturn(List.of(augustFirstNewMembers, augustThirdNewMembers));
        given(adminMemberRepository.countDailyInactiveMembers(startAt, endExclusive))
                .willReturn(List.of(augustSecondInactiveMembers));
        given(adminMemberRepository.countInactiveMembers(startAt, endExclusive))
                .willReturn(1L);

        AdminMemberStatisticsResponse response = adminStatisticsService.getMemberStatistics(period);

        assertThat(response.startDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(response.endDate()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(response.activeMemberCount()).isEqualTo(100L);
        assertThat(response.inactiveMemberCount()).isEqualTo(5L);
        assertThat(response.newMemberCount()).isEqualTo(3L);
        assertThat(response.inactiveProcessedMemberCount()).isEqualTo(1L);
        assertThat(response.dailyStatistics()).containsExactly(
                new AdminMemberStatisticsResponse.DailyStatistics(LocalDate.of(2026, 8, 1), 2L, 0L),
                new AdminMemberStatisticsResponse.DailyStatistics(LocalDate.of(2026, 8, 2), 0L, 1L),
                new AdminMemberStatisticsResponse.DailyStatistics(LocalDate.of(2026, 8, 3), 1L, 0L)
        );
    }

    private DailyCountProjection dailyCount(LocalDate date, Long count) {
        DailyCountProjection projection = mock(DailyCountProjection.class);
        given(projection.getDate()).willReturn(date);
        given(projection.getCount()).willReturn(count);
        return projection;
    }

    private ServiceUsageCountProjection serviceCount(String service, Long count) {
        ServiceUsageCountProjection projection = mock(ServiceUsageCountProjection.class);
        given(projection.getService()).willReturn(service);
        given(projection.getCount()).willReturn(count);
        return projection;
    }

    private DailyServiceUsageProjection dailyServiceCount(String service, LocalDate date, Long count) {
        DailyServiceUsageProjection projection = mock(DailyServiceUsageProjection.class);
        given(projection.getService()).willReturn(service);
        given(projection.getDate()).willReturn(date);
        given(projection.getCount()).willReturn(count);
        return projection;
    }
}
