package com.naengpa.naengpamasterbackend.score.dto.response;

public record ScoreSummaryResponse(
        Long totalGained,
        Long totalLost,
        Long netChange
) {}
