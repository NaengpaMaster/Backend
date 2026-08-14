package com.naengpa.naengpamasterbackend.admin.controller;

import com.naengpa.naengpamasterbackend.admin.dto.response.*;
import com.naengpa.naengpamasterbackend.admin.service.AdminStatisticsService;
import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsPeriod;
import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "관리자 통계", description = "관리자 회원·서비스·재료·레시피 통계 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    // 전체 레시피 현황과 선택 기간 카테고리별 등록 현황 조회
    @Operation(summary = "레시피 통계 조회", description = "선택 기간의 전체 및 카테고리별 레시피 등록 현황을 조회합니다.")
    @GetMapping("/recipes")
    public ResponseEntity<ApiResponse<AdminRecipeStatisticsResponse>> getRecipeStatistics(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getRecipeStatistics(period)));
    }



    // 선택 기간 재료·냉파 상세 통계 조회
    @Operation(summary = "재료·냉파 통계 조회", description = "선택 기간의 재료 등록 및 만료 현황을 조회합니다.")
    @GetMapping("/materials")
    public ResponseEntity<ApiResponse<AdminMaterialStatisticsResponse>> getMaterialStatistics(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getMaterialStatistics(period)));
    }

    // 선택 기간 통계 요약 조회
    @Operation(summary = "통계 요약 조회", description = "선택 기간의 주요 서비스 통계 요약을 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminStatisticsSummaryResponse>> getStatisticsSummary(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getStatisticsSummary(period)));
    }

    // Top 5 유통기한 만료 건수 카테고리 조회
    @Operation(summary = "상위 만료 재료 조회", description = "선택 기간에 가장 많이 만료된 재료 5개를 조회합니다.")
    @GetMapping("/top-ingredients")
    public ResponseEntity<ApiResponse<List<AdminTopWastedIngredientResponse>>> getTop5Ingredients(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getTop5Ingredients(period)));
    }

    // 회원 현황 api
    @Operation(summary = "회원 현황 통계 조회", description = "선택 기간의 신규 가입 및 비활성 처리 회원 현황을 조회합니다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<AdminMemberStatisticsResponse>> getMemberStatistics(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getMemberStatistics(period)));
    }

    // 서비스별 회원 수 이용률 api
    @Operation(summary = "서비스 이용 통계 조회", description = "선택 기간의 냉장고·장보기·레시피 서비스 이용 회원 수와 이용률을 조회합니다.")
    @GetMapping("/service-usage")
    public ResponseEntity<ApiResponse<AdminMemberUsageStatisticsResponse>> getServiceUsage(
            @Parameter(description = "조회 시작일", example = "2026-08-01") @RequestParam LocalDate startDate,
            @Parameter(description = "조회 종료일", example = "2026-08-31") @RequestParam LocalDate endDate
    ) {
        StatisticsPeriod period = StatisticsPeriod.of(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(adminStatisticsService.getMemberUsageStatistics(period)));
    }
}
