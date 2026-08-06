package com.naengpa.naengpamasterbackend.admin.statistics;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public enum StatisticsGranularity {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String sqlUnit;

    StatisticsGranularity(String sqlUnit) {
        this.sqlUnit = sqlUnit;
    }

    public static StatisticsGranularity from(LocalDate startDate, LocalDate endDate) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days <= 31) return DAY;
        if (days <= 365) return WEEK;
        return MONTH;
    }

    public String sqlUnit() {
        return sqlUnit;
    }

    public LocalDate firstBucket(LocalDate date) {
        return switch (this) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    public LocalDate next(LocalDate date) {
        return switch (this) {
            case DAY -> date.plusDays(1);
            case WEEK -> date.plusWeeks(1);
            case MONTH -> date.plusMonths(1);
        };
    }
}
