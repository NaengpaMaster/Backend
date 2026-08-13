package com.naengpa.naengpamasterbackend.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점수 변동 사유별 집계 응답")
public record ScoreByReasonResponse (

    @Schema(
          description = "점수 변동 사유",
          example = "EXPIRED_PRODUCT"
    )
    String scoreReason,

    @Schema(description = "해당 사유 발생 횟수", example = "3")
    Long count,

    @Schema(
            description = "해당 사유로 변경된 점수 총합",
            example = "-6"
    )
    Long totalDelta
){}
