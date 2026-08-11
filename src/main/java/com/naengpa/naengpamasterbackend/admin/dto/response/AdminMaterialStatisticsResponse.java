package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record AdminMaterialStatisticsResponse(
        @Schema(description = "조회 시작일", example = "2026-08-01")
        LocalDate startDate,
        @Schema(description = "조회 종료일", example = "2026-08-31")
        LocalDate endDate,
        @Schema(description = "통계 집계 단위", example = "DAY")
        StatisticsGranularity granularity,
        @Schema(description = "기간 단위별 재료 등록·만료 통계")
        List<DailyStatistics> dailyStatistics,
        @Schema(description = "카테고리별 만료 재료 통계")
        List<AdminCategoryStatResponse> categoryStatistics
) {
    public record DailyStatistics(
            @Schema(description = "집계 구간 시작일", example = "2026-08-01")
            LocalDate date,
            @Schema(description = "등록 재료 수", example = "35")
            Long registeredCount,
            @Schema(description = "만료 재료 수", example = "7")
            Long expiredCount
    ) {
    }
}
