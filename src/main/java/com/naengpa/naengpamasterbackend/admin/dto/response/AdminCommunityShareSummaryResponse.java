package com.naengpa.naengpamasterbackend.admin.dto.response;

public record AdminCommunityShareSummaryResponse(
        long totalPostCount,
        long openPostCount,
        long closedPostCount,
        long cancelledPostCount,
        long participantCount
) {
}
