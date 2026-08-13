package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record AdminMemberUsageStatisticsResponse(
        @Schema(description = "조회 시작일", example = "2026-08-01")
        LocalDate startDate,
        @Schema(description = "조회 종료일", example = "2026-08-31")
        LocalDate endDate,
        @Schema(description = "통계 집계 단위", example = "DAY")
        StatisticsGranularity granularity,
        @Schema(description = "이용률 계산 기준인 현재 활성 회원 수", example = "1200")
        long activeMemberCount,
        @Schema(description = "냉장고 서비스 이용 통계")
        ServiceUsage fridge,
        @Schema(description = "장보기 서비스 이용 통계")
        ServiceUsage shopping,
        @Schema(description = "레시피 등록 서비스 이용 통계")
        ServiceUsage recipe
) {
    public record ServiceUsage(
            @Schema(description = "선택 기간 고유 이용 회원 수", example = "640")
            long userCount,
            @Schema(description = "활성 회원 대비 이용률(%)", example = "53.3")
            double usageRate,
            @Schema(description = "기간 단위별 이용 회원 통계")
            List<DailyUsage> dailyStatistics
    ) {}

    public record DailyUsage(
            @Schema(description = "집계 구간 시작일", example = "2026-08-01")
            LocalDate date,
            @Schema(description = "고유 이용 회원 수", example = "25")
            long userCount
    ) {}
}
