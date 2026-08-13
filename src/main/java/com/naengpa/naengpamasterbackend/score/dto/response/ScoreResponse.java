package com.naengpa.naengpamasterbackend.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 냉파 점수 응답")
public record ScoreResponse(

        @Schema(
                description = "현재 냉파 점수",
                example = "72",
                minimum = "0",
                maximum = "100"
        )
        Integer score

) {}