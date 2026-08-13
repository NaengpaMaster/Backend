package com.naengpa.naengpamasterbackend.fridge.report.dto;

public record WeeklyConsumedProductSummary(
        Long productId,
        String productName,
        long count
) {
}
