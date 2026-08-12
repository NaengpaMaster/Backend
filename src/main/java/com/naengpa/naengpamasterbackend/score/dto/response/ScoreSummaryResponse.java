package com.naengpa.naengpamasterbackend.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이번 달 냉파 점수 변동 요약 응답")
public record ScoreSummaryResponse(

        @Schema(
                description = "이번 달 총 획득 점수",
                example = "15"
        )
        Long totalGained,

        @Schema(
                description = "이번 달 총 감점 점수",
                example = "-6"
        )
        Long totalLost,

        @Schema(
                description = "이번 달 순 점수 변동값",
                example = "9"
        )
        Long netChange

) {}