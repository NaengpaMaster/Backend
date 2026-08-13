package com.naengpa.naengpamasterbackend.admin.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AdminProductUpdateRequest(
        @Schema(description = "사전 재료 카테고리 ID", example = "1")
        @NotNull(message = "카테고리를 선택해주세요.")
        Long productCategoryId,
        @Schema(description = "재료명", example = "두부")
        @NotBlank(message = "재료명을 입력해주세요.")
        String name,
        @Schema(description = "기본 유통기한 일수", example = "7")
        @PositiveOrZero(message = "기본 유통기한은 0일 이상이어야 합니다.")
        Integer defaultExpiryDays
) {
}
