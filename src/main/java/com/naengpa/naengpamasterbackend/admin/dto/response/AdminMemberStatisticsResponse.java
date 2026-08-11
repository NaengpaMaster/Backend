package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record AdminMemberStatisticsResponse(
        @Schema(description = "조회 시작일", example = "2026-08-01")
        LocalDate startDate,
        @Schema(description = "조회 종료일", example = "2026-08-31")
        LocalDate endDate,
        @Schema(description = "통계 집계 단위", example = "DAY")
        StatisticsGranularity granularity,
        @Schema(description = "현재 활성 회원 수", example = "1200")
        long activeMemberCount,
        @Schema(description = "현재 비활성 회원 수", example = "35")
        long inactiveMemberCount,
        @Schema(description = "선택 기간 신규 가입 회원 수", example = "80")
        long newMemberCount,
        @Schema(description = "선택 기간 비활성 처리 고유 회원 수", example = "12")
        long inactiveProcessedMemberCount,
        @Schema(description = "기간 단위별 회원 변동 통계")
        List<DailyStatistics> dailyStatistics
) {
    public record DailyStatistics(
            @Schema(description = "집계 구간 시작일", example = "2026-08-01")
            LocalDate date,
            @Schema(description = "신규 가입 회원 수", example = "3")
            long newMemberCount,
            @Schema(description = "비활성 처리 고유 회원 수", example = "1")
            long inactiveMemberCount
    ) {}

    public static AdminMemberStatisticsResponse of(
            LocalDate startDate,
            LocalDate endDate,
            StatisticsGranularity granularity,
            long activeMemberCount,
            long inactiveMemberCount,
            long newMemberCount,
            long inactiveProcessedMemberCount,
            List<DailyStatistics> dailyStatistics
    ) {
        return new AdminMemberStatisticsResponse(
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
}
