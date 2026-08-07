package com.naengpa.naengpamasterbackend.admin.statistics;

import com.naengpa.naengpamasterbackend.global.exception.InvalidStatisticsPeriodException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StatisticsPeriod(
    LocalDate startDate,
    LocalDate endDate
) {
    public static StatisticsPeriod of(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            throw new InvalidStatisticsPeriodException(
                    "시작일과 종료일은 필수입니다."
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new InvalidStatisticsPeriodException(
                    "시작일은 종료일보다 늦을 수 없습니다."
            );
        }

        return new StatisticsPeriod(startDate, endDate);
    }

    public LocalDateTime startAt() {
        return startDate.atStartOfDay();
    }

    public LocalDateTime endExclusive() {
        return endDate.plusDays(1).atStartOfDay();
    }

    public StatisticsGranularity granularity() {
        return StatisticsGranularity.from(startDate, endDate);
    }
}
