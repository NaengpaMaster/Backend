package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.*;
import com.naengpa.naengpamasterbackend.admin.projection.*;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminScoreRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminStatisticsRepository;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final AdminStatisticsRepository adminStatisticsRepository;
    private final AdminScoreRepository adminScoreRepository;
    private final AdminMemberRepository adminMemberRepository;

    // 전체 레시피 현황과 선택 기간의 카테고리별 등록 현황을 조회합니다.
    @Transactional(readOnly = true)
    public AdminRecipeStatisticsResponse getRecipeStatistics(StatisticsPeriod period) {
        RecipeCountProjection recipeCount = adminStatisticsRepository.countRecipesByCreatorType();
        List<AdminRecipeStatisticsResponse.CategoryStatistics> categoryStatistics =
                adminStatisticsRepository.countRecipesByCategory(
                                period.startAt(), period.endExclusive()
                        )
                        .stream()
                        .map(result -> new AdminRecipeStatisticsResponse.CategoryStatistics(
                                result.getCategoryName(),
                                result.getRecipeCount(),
                                result.getBaseRecipeCount(),
                                result.getMemberRecipeCount(),
                                result.getAdminRecipeCount()
                        ))
                        .toList();

        return new AdminRecipeStatisticsResponse(
                period.startDate(),
                period.endDate(),
                recipeCount.getTotalCount(),
                recipeCount.getBaseCount(),
                recipeCount.getMemberCount(),
                recipeCount.getAdminCount(),
                categoryStatistics
        );
    }

    // 선택 기간의 재료 등록·만료 추이와 카테고리별 만료 현황을 조회합니다.
    @Transactional(readOnly = true)
    public AdminMaterialStatisticsResponse getMaterialStatistics(StatisticsPeriod period) {
        StatisticsGranularity granularity = period.granularity();
        List<AdminMaterialStatisticsResponse.DailyStatistics> dailyStatistics =
                adminStatisticsRepository.findDailyMaterialStatistics(
                                period.startDate(),
                                period.endDate(),
                                period.startAt(),
                                period.endExclusive(),
                                granularity.sqlUnit()
                        )
                        .stream()
                        .map(result -> new AdminMaterialStatisticsResponse.DailyStatistics(
                                result.getDate(),
                                result.getRegisteredCount(),
                                result.getExpiredCount()
                        ))
                        .toList();

        return new AdminMaterialStatisticsResponse(
                period.startDate(),
                period.endDate(),
                granularity,
                dailyStatistics,
                getExpiredCountByCategory(period)
        );
    }

    // 선택 기간의 평균 냉파 점수, 등록·만료 재료 및 신규 레시피 요약을 조회합니다.
    @Transactional(readOnly = true)
    public AdminStatisticsSummaryResponse getStatisticsSummary(StatisticsPeriod period) {
        StatisticsPeriod previousPeriod = previousPeriod(period);
        ScoreAverageProjection scoreAverage = adminScoreRepository.findScoreAverage(
                period.startAt(), period.endExclusive()
        );
        Long registeredIngredientCount = adminStatisticsRepository.countRegisteredIngredients(
                period.startAt(), period.endExclusive()
        );
        Long expiredIngredientCount = adminStatisticsRepository.countByCreatedAtBetween(
                period.startDate(), period.endDate()
        );
        Long previousExpiredIngredientCount = adminStatisticsRepository.countByCreatedAtBetween(
                previousPeriod.startDate(), previousPeriod.endDate()
        );
        Long createdRecipeCount = adminStatisticsRepository.countCreatedRecipes(
                period.startAt(), period.endExclusive()
        );
        Double expiredIngredientChangeRate = previousExpiredIngredientCount == 0
                ? null
                : Math.round(
                        (double) (expiredIngredientCount - previousExpiredIngredientCount)
                                / previousExpiredIngredientCount * 1000
                ) / 10.0;
        double averageScore = Math.round(scoreAverage.getAverageScore() * 10) / 10.0;

        return new AdminStatisticsSummaryResponse(
                averageScore,
                scoreAverage.getMemberCount(),
                registeredIngredientCount,
                expiredIngredientCount,
                previousExpiredIngredientCount,
                expiredIngredientChangeRate,
                createdRecipeCount
        );
    }

    // 선택 기간의 카테고리별 만료 재료 수를 응답 형태로 변환합니다.
    private List<AdminCategoryStatResponse> getExpiredCountByCategory(StatisticsPeriod period) {
        return adminStatisticsRepository.findExpiredCountByCategory(period.startDate(), period.endDate())
                .stream()
                .map(result -> new AdminCategoryStatResponse(
                        result.getCategoryName(),
                        result.getExpiredCount()
                ))
                .toList();
    }

    // 선택 기간의 상위 만료 재료 5개와 이전 기간 대비 순위 변화를 조회합니다.
    @Transactional(readOnly = true)
    public List<AdminTopWastedIngredientResponse> getTop5Ingredients(StatisticsPeriod period) {
        StatisticsPeriod previousPeriod = previousPeriod(period);
        List<ExpiredIngredientProjection> current = adminStatisticsRepository
                .findTop5ExpiredIngredientsBetween(
                        period.startDate(), period.endDate(), PageRequest.of(0, 5)
                );
        List<ExpiredIngredientProjection> previous = adminStatisticsRepository
                .findTop5ExpiredIngredientsBetween(
                        previousPeriod.startDate(), previousPeriod.endDate(), PageRequest.of(0, 5)
                );

        return getAdminTopWastedIngredientResponses(previous, current);
    }

    // 현재·이전 기간의 만료 재료 순위를 비교해 순위 변화 응답을 만듭니다.
    private List<AdminTopWastedIngredientResponse> getAdminTopWastedIngredientResponses(
            List<ExpiredIngredientProjection> previous,
            List<ExpiredIngredientProjection> current
    ) {
        Map<String, Integer> previousRankByName = new HashMap<>();
        for (int i = 0; i < previous.size(); i++) {
            previousRankByName.put(previous.get(i).getProductName(), i + 1);
        }

        List<AdminTopWastedIngredientResponse> list = new ArrayList<>();
        for (int i = 0; i < current.size(); i++) {
            String productName = current.get(i).getProductName();
            int currentRank = i + 1;
            Integer previousRank = previousRankByName.get(productName);
            Integer rankChange = previousRank != null ? previousRank - currentRank : null;

            list.add(new AdminTopWastedIngredientResponse(
                    currentRank,
                    productName,
                    current.get(i).getExpiredCount(),
                    rankChange
            ));
        }
        return list;
    }

    // 선택 기간과 길이가 같은 바로 이전 기간을 계산합니다.
    private StatisticsPeriod previousPeriod(StatisticsPeriod period) {
        long days = ChronoUnit.DAYS.between(period.startDate(), period.endDate()) + 1;
        LocalDate previousEndDate = period.startDate().minusDays(1);

        return StatisticsPeriod.of(previousEndDate.minusDays(days - 1), previousEndDate);
    }

    // 선택 기간의 신규 가입·비활성 처리 회원 현황과 추이를 조회합니다.
    @Transactional(readOnly = true)
    public AdminMemberStatisticsResponse getMemberStatistics(StatisticsPeriod period) {
        LocalDate startDate = period.startDate();
        LocalDate endDate = period.endDate();
        StatisticsGranularity granularity = period.granularity();

        long activeMemberCount = adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER);
        long inactiveMemberCount = adminMemberRepository.countByStatusAndRole(MemberStatus.INACTIVE, MemberRole.USER);

        Map<LocalDate, Long> newMemberCountByDate = toDailyCountMap(
                adminMemberRepository.countDailyNewMembers(
                        period.startAt(), period.endExclusive(), granularity.sqlUnit()
                )
        );
        Map<LocalDate, Long> inactiveMemberCountByDate = toDailyCountMap(
                adminMemberRepository.countDailyInactiveMembers(
                        period.startAt(), period.endExclusive(), granularity.sqlUnit()
                )
        );

        List<AdminMemberStatisticsResponse.DailyStatistics> dailyStatistics = bucketStarts(period, granularity)
                .stream()
                .map(date -> new AdminMemberStatisticsResponse.DailyStatistics(
                        date,
                        newMemberCountByDate.getOrDefault(date, 0L),
                        inactiveMemberCountByDate.getOrDefault(date, 0L)
                ))
                .toList();

        long newMemberCount = newMemberCountByDate.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        long inactiveProcessedMemberCount = adminMemberRepository.countInactiveMembers(
                period.startAt(),
                period.endExclusive()
        );

        return AdminMemberStatisticsResponse.of(
                startDate,
                endDate,
                granularity,
                activeMemberCount,
                inactiveMemberCount,
                newMemberCount,
                inactiveProcessedMemberCount,
                dailyStatistics
        );
    }

    // 선택 기간의 서비스별 이용 회원 수와 이용률 추이를 조회합니다.
    @Transactional(readOnly = true)
    public AdminMemberUsageStatisticsResponse getMemberUsageStatistics(StatisticsPeriod period) {
        StatisticsGranularity granularity = period.granularity();
        long activeMemberCount = adminMemberRepository.countByStatusAndRole(
                MemberStatus.ACTIVE,
                MemberRole.USER
        );
        Map<String, Long> totalCountByService = adminStatisticsRepository
                .countServiceUsageMembers(period.startAt(), period.endExclusive())
                .stream()
                .collect(Collectors.toMap(
                        ServiceUsageCountProjection::getService,
                        ServiceUsageCountProjection::getCount
                ));
        Map<String, Map<LocalDate, Long>> dailyCountByService = adminStatisticsRepository
                .countDailyServiceUsageMembers(
                        period.startAt(), period.endExclusive(), granularity.sqlUnit()
                )
                .stream()
                .collect(Collectors.groupingBy(
                        DailyServiceUsageProjection::getService,
                        Collectors.toMap(
                                DailyServiceUsageProjection::getDate,
                                DailyServiceUsageProjection::getCount
                        )
                ));

        return new AdminMemberUsageStatisticsResponse(
                period.startDate(),
                period.endDate(),
                granularity,
                activeMemberCount,
                createServiceUsage("fridge", period, granularity, activeMemberCount, totalCountByService, dailyCountByService),
                createServiceUsage("shopping", period, granularity, activeMemberCount, totalCountByService, dailyCountByService),
                createServiceUsage("recipe", period, granularity, activeMemberCount, totalCountByService, dailyCountByService)
        );
    }

    // 서비스별 전체·일자별 이용 수를 하나의 응답 객체로 조립합니다.
    private AdminMemberUsageStatisticsResponse.ServiceUsage createServiceUsage(
            String service,
            StatisticsPeriod period,
            StatisticsGranularity granularity,
            long activeMemberCount,
            Map<String, Long> totalCountByService,
            Map<String, Map<LocalDate, Long>> dailyCountByService
    ) {
        long userCount = totalCountByService.getOrDefault(service, 0L);
        double usageRate = activeMemberCount == 0
                ? 0.0
                : Math.round((double) userCount / activeMemberCount * 1000) / 10.0;
        Map<LocalDate, Long> dailyCounts = dailyCountByService.getOrDefault(service, Map.of());
        List<AdminMemberUsageStatisticsResponse.DailyUsage> dailyStatistics = bucketStarts(period, granularity)
                .stream()
                .map(date -> new AdminMemberUsageStatisticsResponse.DailyUsage(
                        date,
                        dailyCounts.getOrDefault(date, 0L)
                ))
                .toList();

        return new AdminMemberUsageStatisticsResponse.ServiceUsage(
                userCount,
                usageRate,
                dailyStatistics
        );
    }

    // 일별 집계 결과를 날짜로 빠르게 조회할 수 있는 Map으로 변환합니다.
    private Map<LocalDate, Long> toDailyCountMap(List<DailyCountProjection> projections) {
        Map<LocalDate, Long> countByDate = new HashMap<>();
        projections.forEach(projection -> countByDate.put(projection.getDate(), projection.getCount()));
        return countByDate;
    }

    // 조회 기간과 집계 단위에 맞는 일·주·월별 구간 시작일을 생성합니다.
    private List<LocalDate> bucketStarts(
            StatisticsPeriod period,
            StatisticsGranularity granularity
    ) {
        LocalDate firstBucket = granularity.firstBucket(period.startDate());
        LocalDate lastBucket = granularity.firstBucket(period.endDate());

        return java.util.stream.Stream.iterate(
                        firstBucket,
                        date -> !date.isAfter(lastBucket),
                        granularity::next
                )
                .toList();
    }
}
