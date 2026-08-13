package com.naengpa.naengpamasterbackend.shopping.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ShoppingItemListResponse(
        @Schema(description = "장보기 항목 ID", example = "1")
        Long shoppingItemId,
        @Schema(description = "사전 재료 ID", example = "1")
        Long productId,
        @Schema(description = "사전 재료 카테고리 ID", example = "1")
        Long productCategoryId,
        @Schema(description = "재료명", example = "감자")
        String productName,
        @Schema(description = "수량", example = "2개")
        String quantity,
        @Schema(description = "구매 여부", example = "false")
        Boolean isPurchased
) {

}
