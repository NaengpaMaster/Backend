package com.naengpa.naengpamasterbackend.fridge.report.dto;

import java.math.BigDecimal;

public record WeeklyConsumedCategorySummary(
        Long productCategoryId,
        String categoryName,
        long count,
        BigDecimal ratio
) {
}
