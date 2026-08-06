package com.naengpa.naengpamasterbackend.admin.projection;

import java.time.LocalDate;

public interface DailyCountProjection {
    LocalDate getDate();
    Long getCount();
}
