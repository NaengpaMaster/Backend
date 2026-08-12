package com.naengpa.naengpamasterbackend.fridge.report.dto;

import java.time.LocalDate;

public record WeeklyRemainingFridgeItemSummary(
        Long fridgeItemId,
        String productName,
        String quantity,
        LocalDate expiryDate
) {
}
