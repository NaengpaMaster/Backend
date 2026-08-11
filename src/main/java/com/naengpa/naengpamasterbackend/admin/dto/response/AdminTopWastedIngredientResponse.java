package com.naengpa.naengpamasterbackend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminTopWastedIngredientResponse(
        @Schema(description = "현재 만료 순위", example = "1") Integer rank,
        @Schema(description = "재료명", example = "대파") String productName,
        @Schema(description = "만료 건수", example = "18") Long discardedCount,
        @Schema(description = "이전 동일 기간 대비 순위 변화", example = "2", nullable = true) Integer rankChange
) {
}
