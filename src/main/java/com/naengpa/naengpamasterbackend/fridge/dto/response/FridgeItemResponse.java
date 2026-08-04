package com.naengpa.naengpamasterbackend.fridge.dto.response;

import com.naengpa.naengpamasterbackend.fridge.entity.FridgeItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record FridgeItemResponse(
        @Schema(description = "냉장고 재료 ID", example = "1")
        Long fridgeItemId,
        @Schema(description = "사전 재료 ID", example = "1")
        Long productId,
        @Schema(description = "수량", example = "1개")
        String quantity,
        @Schema(description = "유통기한", example = "2026-08-04")
        LocalDate expiryDate,
        @Schema(description = "메모", example = "아침용")
        String memo
) {
    public static FridgeItemResponse from(FridgeItem fridgeItem) {
        return new FridgeItemResponse(
                fridgeItem.getFridgeItemId(),
                fridgeItem.getProductId(),
                fridgeItem.getQuantity(),
                fridgeItem.getExpiryDate(),
                fridgeItem.getMemo()
        );
    }
}
