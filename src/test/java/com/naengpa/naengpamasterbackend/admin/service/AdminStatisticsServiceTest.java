package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMemberUsageStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminMaterialStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminRecipeStatisticsResponse;
import com.naengpa.naengpamasterbackend.admin.dto.response.AdminStatisticsSummaryResponse;
import com.naengpa.naengpamasterbackend.admin.projection.DailyCountProjection;
import com.naengpa.naengpamasterbackend.admin.projection.DailyMaterialStatisticsProjection;
import com.naengpa.naengpamasterbackend.admin.projection.DailyServiceUsageProjection;
import com.naengpa.naengpamasterbackend.admin.projection.ServiceUsageCountProjection;
import com.naengpa.naengpamasterbackend.admin.projection.ScoreAverageProjection;
import com.naengpa.naengpamasterbackend.admin.projection.RecipeCountProjection;
import com.naengpa.naengpamasterbackend.admin.projection.RecipeCategoryCountProjection;
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
    private AdminScoreRepository adminScoreRepository;
    private AdminStatisticsService adminStatisticsService;

    @BeforeEach
    void setUp() {
        adminMemberRepository = mock(AdminMemberRepository.class);
        adminStatisticsRepository = mock(AdminStatisticsRepository.class);
        adminScoreRepository = mock(AdminScoreRepository.class);
        adminStatisticsService = new AdminStatisticsService(
                adminStatisticsRepository,
                adminScoreRepository,
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
        given(adminStatisticsRepository.countDailyServiceUsageMembers(
                period.startAt(), period.endExclusive(), "day"
        )).willReturn(List.of(fridgeFirst, fridgeThird, shoppingSecond));

        AdminMemberUsageStatisticsResponse response =
                adminStatisticsService.getMemberUsageStatistics(period);

        assertThat(response.granularity()).isEqualTo(
                com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity.DAY
        );
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
    void getStatisticsSummaryCalculatesExpiredIngredientChangeRate() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3)
        );
        given(adminStatisticsRepository.countRegisteredIngredients(
                period.startAt(), period.endExclusive()
        )).willReturn(40L);
        ScoreAverageProjection scoreAverage = scoreAverage(82.54, 30L);
        given(adminScoreRepository.findScoreAverage(period.startAt(), period.endExclusive()))
                .willReturn(scoreAverage);
        given(adminStatisticsRepository.countByCreatedAtBetween(
                period.startDate(), period.endDate()
        )).willReturn(10L);
        given(adminStatisticsRepository.countByCreatedAtBetween(
                LocalDate.of(2026, 7, 29), LocalDate.of(2026, 7, 31)
        )).willReturn(8L);
        given(adminStatisticsRepository.countCreatedRecipes(
                period.startAt(), period.endExclusive()
        )).willReturn(7L);

        AdminStatisticsSummaryResponse response = adminStatisticsService.getStatisticsSummary(period);

        assertThat(response).isEqualTo(
                new AdminStatisticsSummaryResponse(82.5, 30L, 40L, 10L, 8L, 25.0, 7L)
        );
    }

    @Test
    void getStatisticsSummaryReturnsNullChangeRateWhenPreviousPeriodIsEmpty() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)
        );
        ScoreAverageProjection scoreAverage = scoreAverage(0.0, 0L);
        given(adminScoreRepository.findScoreAverage(period.startAt(), period.endExclusive()))
                .willReturn(scoreAverage);
        given(adminStatisticsRepository.countRegisteredIngredients(
                period.startAt(), period.endExclusive()
        )).willReturn(0L);
        given(adminStatisticsRepository.countByCreatedAtBetween(
                period.startDate(), period.endDate()
        )).willReturn(3L);
        given(adminStatisticsRepository.countByCreatedAtBetween(
                LocalDate.of(2026, 7, 29), LocalDate.of(2026, 7, 31)
        )).willReturn(0L);
        given(adminStatisticsRepository.countCreatedRecipes(
                period.startAt(), period.endExclusive()
        )).willReturn(0L);

        AdminStatisticsSummaryResponse response = adminStatisticsService.getStatisticsSummary(period);

        assertThat(response.expiredIngredientChangeRate()).isNull();
    }

    @Test
    void getMaterialStatisticsReturnsDailyZeroValuesFromProjection() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)
        );
        DailyMaterialStatisticsProjection firstDay = dailyMaterialStatistics(
                LocalDate.of(2026, 8, 1), 2L, 1L
        );
        DailyMaterialStatisticsProjection secondDay = dailyMaterialStatistics(
                LocalDate.of(2026, 8, 2), 0L, 0L
        );
        given(adminStatisticsRepository.findDailyMaterialStatistics(
                period.startDate(), period.endDate(), period.startAt(), period.endExclusive(), "day"
        )).willReturn(List.of(firstDay, secondDay));
        given(adminStatisticsRepository.findExpiredCountByCategory(
                period.startDate(), period.endDate()
        )).willReturn(List.of());

        AdminMaterialStatisticsResponse response =
                adminStatisticsService.getMaterialStatistics(period);

        assertThat(response.granularity()).isEqualTo(
                com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity.DAY
        );
        assertThat(response.dailyStatistics()).containsExactly(
                new AdminMaterialStatisticsResponse.DailyStatistics(
                        LocalDate.of(2026, 8, 1), 2L, 1L
                ),
                new AdminMaterialStatisticsResponse.DailyStatistics(
                        LocalDate.of(2026, 8, 2), 0L, 0L
                )
        );
    }

    @Test
    void getRecipeStatisticsSeparatesCreatorTypesAndReturnsCategoryCounts() {
        StatisticsPeriod period = StatisticsPeriod.of(
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2)
        );
        RecipeCountProjection totals = recipeCount(100L, 60L, 30L, 10L);
        RecipeCategoryCountProjection koreanCategory = recipeCategoryCount(
                "한식", 5L, 2L, 2L, 1L
        );
        given(adminStatisticsRepository.countRecipesByCreatorType()).willReturn(totals);
        given(adminStatisticsRepository.countRecipesByCategory(
                period.startAt(), period.endExclusive()
        )).willReturn(List.of(koreanCategory));

        AdminRecipeStatisticsResponse response =
                adminStatisticsService.getRecipeStatistics(period);

        assertThat(response.totalRecipeCount()).isEqualTo(100L);
        assertThat(response.baseRecipeCount()).isEqualTo(60L);
        assertThat(response.memberRecipeCount()).isEqualTo(30L);
        assertThat(response.adminRecipeCount()).isEqualTo(10L);
        assertThat(response.categoryStatistics()).containsExactly(
                new AdminRecipeStatisticsResponse.CategoryStatistics(
                        "한식", 5L, 2L, 2L, 1L
                )
        );
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
        given(adminMemberRepository.countDailyNewMembers(startAt, endExclusive, "day"))
                .willReturn(List.of(augustFirstNewMembers, augustThirdNewMembers));
        given(adminMemberRepository.countDailyInactiveMembers(startAt, endExclusive, "day"))
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

    private RecipeCategoryCountProjection recipeCategoryCount(
            String categoryName,
            Long recipeCount,
            Long baseRecipeCount,
            Long memberRecipeCount,
            Long adminRecipeCount
    ) {
        RecipeCategoryCountProjection projection = mock(RecipeCategoryCountProjection.class);
        given(projection.getCategoryName()).willReturn(categoryName);
        given(projection.getRecipeCount()).willReturn(recipeCount);
        given(projection.getBaseRecipeCount()).willReturn(baseRecipeCount);
        given(projection.getMemberRecipeCount()).willReturn(memberRecipeCount);
        given(projection.getAdminRecipeCount()).willReturn(adminRecipeCount);
        return projection;
    }

    private ScoreAverageProjection scoreAverage(Double averageScore, Long memberCount) {
        ScoreAverageProjection projection = mock(ScoreAverageProjection.class);
        given(projection.getAverageScore()).willReturn(averageScore);
        given(projection.getMemberCount()).willReturn(memberCount);
        return projection;
    }

    private DailyMaterialStatisticsProjection dailyMaterialStatistics(
            LocalDate date,
            Long registeredCount,
            Long expiredCount
    ) {
        DailyMaterialStatisticsProjection projection = mock(DailyMaterialStatisticsProjection.class);
        given(projection.getDate()).willReturn(date);
        given(projection.getRegisteredCount()).willReturn(registeredCount);
        given(projection.getExpiredCount()).willReturn(expiredCount);
        return projection;
    }

    private RecipeCountProjection recipeCount(
            Long totalCount,
            Long baseCount,
            Long memberCount,
            Long adminCount
    ) {
        RecipeCountProjection projection = mock(RecipeCountProjection.class);
        given(projection.getTotalCount()).willReturn(totalCount);
        given(projection.getBaseCount()).willReturn(baseCount);
        given(projection.getMemberCount()).willReturn(memberCount);
        given(projection.getAdminCount()).willReturn(adminCount);
        return projection;
    }

}
