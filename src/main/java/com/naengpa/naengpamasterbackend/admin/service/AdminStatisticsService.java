package com.naengpa.naengpamasterbackend.admin.service;

import com.naengpa.naengpamasterbackend.admin.dto.response.*;
import com.naengpa.naengpamasterbackend.admin.projection.DailyCountProjection;
import com.naengpa.naengpamasterbackend.admin.projection.DailyServiceUsageProjection;
import com.naengpa.naengpamasterbackend.admin.projection.ServiceUsageCountProjection;
import com.naengpa.naengpamasterbackend.admin.repository.AdminMemberRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminScoreRepository;
import com.naengpa.naengpamasterbackend.admin.repository.AdminStatisticsRepository;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.member.entity.MemberRole;
import com.naengpa.naengpamasterbackend.member.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

    // 냉파 점수 평균 조회
    @Transactional(readOnly = true)
    public AdminScoreAverageResponse getScoreAverage() {
        Long activeUserCount = adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER);
        Double scoreAverage = adminScoreRepository.findScoreAverage();

        if (scoreAverage == null) {
            scoreAverage = 0.0;
        } else {
            scoreAverage = Math.round(scoreAverage * 10) / 10.0; // 소수점 첫째 자리까지 반올림
        }

        return new AdminScoreAverageResponse(scoreAverage, activeUserCount);
    }

    // 유통기한 만료 건수 조회
    @Transactional(readOnly = true)
    public AdminExpiredCountResponse getExpiredCount() {
        LocalDate startOfWeek  = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY);

        LocalDate startOfLastWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfLastWeek = endOfWeek.minusWeeks(1);

        Long thisWeekCount = adminStatisticsRepository.countByCreatedAtBetween(startOfWeek, endOfWeek);
        Long lastWeekCount = adminStatisticsRepository.countByCreatedAtBetween(startOfLastWeek, endOfLastWeek);

        Double weekChangePct = lastWeekCount == 0 ? null : Math.round(((double) (thisWeekCount - lastWeekCount) / lastWeekCount) * 100 * 10) / 10.0;

        return new AdminExpiredCountResponse(thisWeekCount, lastWeekCount, weekChangePct);
    }

    // 카테고리별 만료량 조회
    @Transactional(readOnly = true)
    public List<AdminCategoryStatResponse> getExpiredCountByCategory(Integer period) {
        LocalDate startDate = switch (period) {
            case 7   -> LocalDate.now().minusDays(7);
            case 30 -> LocalDate.now().minusDays(30);
            case 0  -> LocalDate.of(1970, 1, 1); // 전체 기간
            default -> throw new IllegalArgumentException("period값은 7, 30, 0(전체) 중 하나여야 합니다.");
        };

        return adminStatisticsRepository.findExpiredCountByCategory(startDate)
                .stream()
                .map(row -> new AdminCategoryStatResponse(
                        (String) row[0],
                        (Long) row[1]
                ))
                .toList();
    }

    // top 5 유통기한 만료 재료 조회
    @Transactional(readOnly = true)
    public List<AdminTopWastedIngredientResponse> getTop5Ingredients() {
        LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate thisWeekEnd = LocalDate.now();

        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate lastWeekEnd = thisWeekStart.minusDays(1);

        // 이번주 TOP5
        List<Object[]> thisWeek = adminStatisticsRepository
                .findTop5ExpiredIngredientsBetween(thisWeekStart, thisWeekEnd, PageRequest.of(0, 5));

        // 지난주 TOP5 → 순위 Map
        List<Object[]> lastWeek = adminStatisticsRepository
                .findTop5ExpiredIngredientsBetween(lastWeekStart, lastWeekEnd, PageRequest.of(0, 5));

        return getAdminTopWastedIngredientResponses(lastWeek, thisWeek);
    }

    // 주간 만료 추이 (날짜별 만료 건수)
    @Transactional(readOnly = true)
    public AdminWeeklyTrendResponse getWeeklyExpiredTrend() {
        ArrayList<AdminWeeklyData> weeks = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate start = LocalDate.now().minusWeeks(i).with(DayOfWeek.MONDAY);
            LocalDate end = LocalDate.now().minusWeeks(i).with(DayOfWeek.SUNDAY);
            Long count = adminStatisticsRepository.countByCreatedAtBetween(start, end);

            String label = switch (i) {
                case 0 -> "이번주";
                case 1 -> "지난주";
                default -> i + "주전";
            };

            weeks.add(new AdminWeeklyData(label, count));
        }

       return new AdminWeeklyTrendResponse(weeks);
    }

    private List<AdminTopWastedIngredientResponse> getAdminTopWastedIngredientResponses(List<Object[]> lastWeek, List<Object[]> thisWeek) {
        Map<String, Integer> lastWeekRankMap = new HashMap<>();
        for (int i = 0; i < lastWeek.size(); i++) {
            lastWeekRankMap.put((String) lastWeek.get(i)[0], i + 1);
        }

        // 이번주 순위 + rankChange 계산
        List<AdminTopWastedIngredientResponse> list = new ArrayList<>();
        for (int i = 0; i < thisWeek.size(); i++) {
            String productName = (String) thisWeek.get(i)[0];
            Long count = (Long) thisWeek.get(i)[1];
            int currentRank = i + 1;

            Integer lastRank = lastWeekRankMap.get(productName);
            Integer rankChange = lastRank != null ? lastRank - currentRank : null;

            list.add(new AdminTopWastedIngredientResponse(
                    currentRank,
                    productName,
                    count,
                    rankChange
            ));
        }
        return list;
    }

    // 회원 현황 통계 api
    @Transactional(readOnly = true)
    public AdminMemberStatisticsResponse getMemberStatistics(StatisticsPeriod period) {
        LocalDate startDate = period.startDate();
        LocalDate endDate = period.endDate();

        long activeMemberCount = adminMemberRepository.countByStatusAndRole(MemberStatus.ACTIVE, MemberRole.USER);
        long inactiveMemberCount = adminMemberRepository.countByStatusAndRole(MemberStatus.INACTIVE, MemberRole.USER);

        Map<LocalDate, Long> newMemberCountByDate = toDailyCountMap(
                adminMemberRepository.countDailyNewMembers(period.startAt(), period.endExclusive())
        );
        Map<LocalDate, Long> inactiveMemberCountByDate = toDailyCountMap(
                adminMemberRepository.countDailyInactiveMembers(period.startAt(), period.endExclusive())
        );

        List<AdminMemberStatisticsResponse.DailyStatistics> dailyStatistics = startDate
                .datesUntil(endDate.plusDays(1))
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
                activeMemberCount,
                inactiveMemberCount,
                newMemberCount,
                inactiveProcessedMemberCount,
                dailyStatistics
        );
    }

    @Transactional(readOnly = true)
    public AdminMemberUsageStatisticsResponse getMemberUsageStatistics(StatisticsPeriod period) {
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
                .countDailyServiceUsageMembers(period.startAt(), period.endExclusive())
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
                activeMemberCount,
                createServiceUsage("fridge", period, activeMemberCount, totalCountByService, dailyCountByService),
                createServiceUsage("shopping", period, activeMemberCount, totalCountByService, dailyCountByService),
                createServiceUsage("recipe", period, activeMemberCount, totalCountByService, dailyCountByService)
        );
    }

    private AdminMemberUsageStatisticsResponse.ServiceUsage createServiceUsage(
            String service,
            StatisticsPeriod period,
            long activeMemberCount,
            Map<String, Long> totalCountByService,
            Map<String, Map<LocalDate, Long>> dailyCountByService
    ) {
        long userCount = totalCountByService.getOrDefault(service, 0L);
        double usageRate = activeMemberCount == 0
                ? 0.0
                : Math.round((double) userCount / activeMemberCount * 1000) / 10.0;
        Map<LocalDate, Long> dailyCounts = dailyCountByService.getOrDefault(service, Map.of());
        List<AdminMemberUsageStatisticsResponse.DailyUsage> dailyStatistics = period.startDate()
                .datesUntil(period.endDate().plusDays(1))
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

    private Map<LocalDate, Long> toDailyCountMap(List<DailyCountProjection> projections) {
        Map<LocalDate, Long> countByDate = new HashMap<>();
        projections.forEach(projection -> countByDate.put(projection.getDate(), projection.getCount()));
        return countByDate;
    }
}
