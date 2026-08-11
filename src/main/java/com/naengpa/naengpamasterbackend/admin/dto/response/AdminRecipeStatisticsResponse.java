package com.naengpa.naengpamasterbackend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record AdminRecipeStatisticsResponse(
        @Schema(description = "조회 시작일", example = "2026-08-01")
        LocalDate startDate,
        @Schema(description = "조회 종료일", example = "2026-08-31")
        LocalDate endDate,
        @Schema(description = "전체 레시피 수", example = "340")
        Long totalRecipeCount,
        @Schema(description = "기본 제공 레시피 수", example = "180")
        Long baseRecipeCount,
        @Schema(description = "회원 등록 레시피 수", example = "120")
        Long memberRecipeCount,
        @Schema(description = "관리자 등록 레시피 수", example = "40")
        Long adminRecipeCount,
        @Schema(description = "카테고리별 레시피 등록 통계")
        List<CategoryStatistics> categoryStatistics
) {
    public record CategoryStatistics(
            @Schema(description = "레시피 카테고리명", example = "한식")
            String categoryName,
            @Schema(description = "카테고리 전체 레시피 수", example = "80")
            Long recipeCount,
            @Schema(description = "기본 제공 레시피 수", example = "40")
            Long baseRecipeCount,
            @Schema(description = "회원 등록 레시피 수", example = "30")
            Long memberRecipeCount,
            @Schema(description = "관리자 등록 레시피 수", example = "10")
            Long adminRecipeCount
    ) {
    }
}
