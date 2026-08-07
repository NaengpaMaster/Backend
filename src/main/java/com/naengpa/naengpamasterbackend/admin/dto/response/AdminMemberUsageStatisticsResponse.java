package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.admin.statistics.StatisticsGranularity;
import java.time.LocalDate;
import java.util.List;

public record AdminMemberUsageStatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        StatisticsGranularity granularity,
        long activeMemberCount,
        ServiceUsage fridge,
        ServiceUsage shopping,
        ServiceUsage recipe
) {
    public record ServiceUsage(
            long userCount,
            double usageRate,
            List<DailyUsage> dailyStatistics
    ) {}

    public record DailyUsage(
            LocalDate date,
            long userCount
    ) {}
}
