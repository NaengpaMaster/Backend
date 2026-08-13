package com.naengpa.naengpamasterbackend.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminCategoryStatResponse(
        @Schema(description = "재료 카테고리명", example = "채소") String categoryName,
        @Schema(description = "카테고리의 만료 재료 수", example = "25") Long expiredCount
) {
}
