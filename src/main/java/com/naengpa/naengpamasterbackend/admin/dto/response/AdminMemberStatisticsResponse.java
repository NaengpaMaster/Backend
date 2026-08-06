package com.naengpa.naengpamasterbackend.admin.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AdminMemberStatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        long activeMemberCount,
        long inactiveMemberCount,
        long newMemberCount,
        long inactiveProcessedMemberCount,
        List<DailyStatistics> dailyStatistics
) {
    public record DailyStatistics(
            LocalDate date,
            long newMemberCount,
            long inactiveMemberCount
    ) {}

    public static AdminMemberStatisticsResponse of(
            LocalDate startDate,
            LocalDate endDate,
            long activeMemberCount,
            long inactiveMemberCount,
            long newMemberCount,
            long inactiveProcessedMemberCount,
            List<DailyStatistics> dailyStatistics
    ) {
        return new AdminMemberStatisticsResponse(
                startDate,
                endDate,
                activeMemberCount,
                inactiveMemberCount,
                newMemberCount,
                inactiveProcessedMemberCount,
                dailyStatistics
        );
    }
}