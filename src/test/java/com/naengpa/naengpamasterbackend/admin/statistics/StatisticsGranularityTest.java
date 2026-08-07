package com.naengpa.naengpamasterbackend.admin.statistics;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsGranularityTest {

    @Test
    void selectsGranularityByPeriodLength() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        assertThat(StatisticsGranularity.from(startDate, startDate.plusDays(30)))
                .isEqualTo(StatisticsGranularity.DAY);
        assertThat(StatisticsGranularity.from(startDate, startDate.plusDays(31)))
                .isEqualTo(StatisticsGranularity.WEEK);
        assertThat(StatisticsGranularity.from(startDate, startDate.plusDays(365)))
                .isEqualTo(StatisticsGranularity.MONTH);
    }
}
