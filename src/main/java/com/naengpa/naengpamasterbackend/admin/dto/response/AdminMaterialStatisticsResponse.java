package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
import java.time.LocalDate;
import java.util.List;

public record AdminMaterialStatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        StatisticsGranularity granularity,
        List<DailyStatistics> dailyStatistics,
        List<AdminCategoryStatResponse> categoryStatistics
) {
    public record DailyStatistics(
            LocalDate date,
            Long registeredCount,
            Long expiredCount
    ) {
    }
}
