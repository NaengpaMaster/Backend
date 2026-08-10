package com.naengpa.naengpamasterbackend.admin.dto.response;

import java.time.LocalDateTime;

public record AdminHomeResponse(
        long activeMemberCount,
        long inactiveMemberCount,
        long todayNewMemberCount,
        long todayInactiveMemberCount,
        long pendingInquiryCount,
        long overduePendingInquiryCount,
        long totalRecipeCount,
        long memberRecipeCount,
        long activeProductCount,
        long inactiveProductCount,
        LocalDateTime refreshedAt
) {
}
