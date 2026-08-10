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

    // 전체 레시피 현황과 선택 기간 카테고리별 등록 현황 조회
    @GetMapping("/recipes")
    public ResponseEntity<ApiResponse<AdminRecipeStatisticsResponse>> getRecipeStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getRecipeStatistics(period)));
    }

    // 선택 기간 재료·냉파 상세 통계 조회
    @GetMapping("/materials")
    public ResponseEntity<ApiResponse<AdminMaterialStatisticsResponse>> getMaterialStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getMaterialStatistics(period)));
    }

    // 선택 기간 통계 요약 조회
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminStatisticsSummaryResponse>> getStatisticsSummary(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getStatisticsSummary(period)));
    }

    // Top 5 유통기한 만료 건수 카테고리 조회
    @GetMapping("/top-ingredients")
    public ResponseEntity<ApiResponse<List<AdminTopWastedIngredientResponse>>> getTop5Ingredients(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getTop5Ingredients(period)));
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
