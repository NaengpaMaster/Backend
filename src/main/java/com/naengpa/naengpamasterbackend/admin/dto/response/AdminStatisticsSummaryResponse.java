package com.naengpa.naengpamasterbackend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminStatisticsSummaryResponse(
        @Schema(description = "선택 기간 평균 냉파 점수", example = "72.5")
        Double averageScore,
        @Schema(description = "평균 냉파 점수 집계 회원 수", example = "850")
        Long scoreMemberCount,
        @Schema(description = "선택 기간 등록 재료 수", example = "520")
        Long registeredIngredientCount,
        @Schema(description = "선택 기간 만료 재료 수", example = "80")
        Long expiredIngredientCount,
        @Schema(description = "이전 동일 기간 만료 재료 수", example = "70")
        Long previousExpiredIngredientCount,
        @Schema(description = "이전 동일 기간 대비 만료 재료 증감률(%)", example = "14.3", nullable = true)
        Double expiredIngredientChangeRate,
        @Schema(description = "선택 기간 신규 레시피 수", example = "32")
        Long createdRecipeCount
) {
}
