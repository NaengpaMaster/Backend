package com.naengpa.naengpamasterbackend.fridge.report.dto;

public record WeeklyFridgeReportRecipient(
        Long fridgeId,
        String fridgeName,
        Long receiverMemberId,
        String receiverEmail
) {
}
