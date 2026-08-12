package com.naengpa.naengpamasterbackend.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가장 많이 만료된 식재료 TOP 5 응답")
public record TopIngredientResponse(

        @Schema(description = "만료 순위", example = "1")
        int rank,

        @Schema(description = "식재료명", example = "우유")
        String ingredientName,

        @Schema(description = "만료 건수", example = "8")
        long expiredCount

) {}