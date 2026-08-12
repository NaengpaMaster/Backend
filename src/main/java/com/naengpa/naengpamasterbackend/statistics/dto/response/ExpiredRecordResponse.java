package com.naengpa.naengpamasterbackend.statistics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "최근 식재료 만료 기록 응답")
public record ExpiredRecordResponse(

        @Schema(description = "만료된 식재료명", example = "우유")
        String ingredientName,

        @Schema(description = "식재료 카테고리 ID", example = "3")
        Long productCategoryId,

        @Schema(
                description = "만료 이력 기록일",
                example = "2026-08-12"
        )
        LocalDate expiredDate

) {}