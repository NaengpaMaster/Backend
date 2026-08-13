package com.naengpa.naengpamasterbackend.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리별 만료 식재료 통계 응답")
public record ExpiredProductCategoryResponse(

        @Schema(description = "식재료 카테고리명", example = "유제품")
        String categoryName,

        @Schema(description = "해당 카테고리의 만료 건수", example = "5")
        Long expiredCount

) {}