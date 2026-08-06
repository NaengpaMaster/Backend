package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.*;
import com.naengpa.naengpamasterbackend.admin.service.AdminStatisticsService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    // 냉파 점수 평균
    @GetMapping("/score-average")
    public ResponseEntity<ApiResponse<AdminScoreAverageResponse>> getScoreAverage() {

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getScoreAverage()));
    }

    // 유통기한 만료 건수 조회
    @GetMapping("/expired-count")
    public ResponseEntity<ApiResponse<AdminExpiredCountResponse>> getExpiredCount() {
        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getExpiredCount()));
    }

    // 카테고리별 유통기한 만료 건수 조회
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<AdminCategoryStatResponse>>> getExpiredCountByCategory(
            @RequestParam(required = true) Integer period
    ) {
        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getExpiredCountByCategory(period)));
    }

    // Top 5 유통기한 만료 건수 카테고리 조회
    @GetMapping("/top-ingredients")
    public ResponseEntity<ApiResponse<List<AdminTopWastedIngredientResponse>>> getTop5Ingredients() {
        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getTop5Ingredients()));
    }

    // 주간 만료 추이 (날짜별 만료 건수)
    @GetMapping("/weekly-trend")
    public ResponseEntity<ApiResponse<AdminWeeklyTrendResponse>> getWeeklyExpiredTrend() {
        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getWeeklyExpiredTrend()));
    }

    // 회원 현황 api
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<AdminMemberStatisticsResponse>> getMemberStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getMemberStatistics(period)));
    }

    // 서비스별 회원 수 이용률 api
    @GetMapping("/service-usage")
    public ResponseEntity<ApiResponse<AdminMemberUsageStatisticsResponse>> getServiceUsage(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getMemberUsageStatistics(period)));
    }
}
