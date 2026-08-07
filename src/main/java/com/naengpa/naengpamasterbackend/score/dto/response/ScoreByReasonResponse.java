package com.naengpa.naengpamasterbackend.score.dto.response;

public record ScoreByReasonResponse (
    String scoreReason,
    Long count,
    Long totalDelta
){}
