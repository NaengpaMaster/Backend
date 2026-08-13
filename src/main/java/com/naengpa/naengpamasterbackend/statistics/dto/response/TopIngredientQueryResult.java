package com.naengpa.naengpamasterbackend.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "TOP 만료 식재료 조회용 내부 쿼리 결과")
public record TopIngredientQueryResult(

        @Schema(description = "식재료명", example = "우유")
        String ingredientName,

        @Schema(description = "만료 건수", example = "8")
        Long expiredCount

) {}