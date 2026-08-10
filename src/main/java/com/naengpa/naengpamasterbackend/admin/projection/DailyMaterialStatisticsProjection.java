package com.naengpa.naengpamasterbackend.admin.projection;

import java.time.LocalDate;

public interface DailyMaterialStatisticsProjection {

    LocalDate getDate();

    Long getRegisteredCount();

    Long getExpiredCount();
}
