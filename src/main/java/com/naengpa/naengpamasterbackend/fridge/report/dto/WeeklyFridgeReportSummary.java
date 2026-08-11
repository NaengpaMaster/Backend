package com.naengpa.naengpamasterbackend.fridge.report.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyFridgeReportSummary(
        Long fridgeId,
        String fridgeName,
        LocalDate startDate,
        LocalDate endDate,
        long totalConsumedCount,
        List<WeeklyConsumedProductSummary> topProducts,
        List<WeeklyConsumedCategorySummary> categories,
        List<WeeklyRemainingFridgeItemSummary> remainingItems
) {

    public boolean hasConsumption() {
        return totalConsumedCount > 0;
    }
}
