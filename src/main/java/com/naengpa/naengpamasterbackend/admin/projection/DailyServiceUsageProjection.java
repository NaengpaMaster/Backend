package com.naengpa.naengpamasterbackend.admin.projection;

import java.time.LocalDate;

public interface DailyServiceUsageProjection {
    String getService();
    LocalDate getDate();
    Long getCount();
}
