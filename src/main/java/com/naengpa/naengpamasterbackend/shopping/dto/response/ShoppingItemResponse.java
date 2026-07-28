package com.naengpa.naengpamasterbackend.shopping.dto.response;

import com.naengpa.naengpamasterbackend.shopping.entity.ShoppingItem;
import io.swagger.v3.oas.annotations.media.Schema;


public record ShoppingItemResponse (
        @Schema(description = "장보기 항목 ID", example = "1")
        Long shoppingItemId,
        @Schema(description = "회원 ID", example = "1")
        Long memberId,
        @Schema(description = "사전 재료 ID", example = "1")
        Long productId,
        @Schema(description = "수량", example = "1개")
        String quantity,
        @Schema(description = "구매 여부", example = "false")
        Boolean isPurchased
){
    public static ShoppingItemResponse from(ShoppingItem shoppingItem) {
        return new ShoppingItemResponse(
                shoppingItem.getShoppingItemId(),
                shoppingItem.getMemberId(),
                shoppingItem.getProductId(),
                shoppingItem.getQuantity(),
                shoppingItem.getIsPurchased()
        );
    }
}
