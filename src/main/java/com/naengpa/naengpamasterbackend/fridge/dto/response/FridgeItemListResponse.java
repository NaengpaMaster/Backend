package com.naengpa.naengpamasterbackend.fridge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record FridgeItemListResponse (
        @Schema(description = "냉장고 재료 ID", example = "1")
        Long fridgeItemId,
        @Schema(description = "사전 재료 ID", example = "1")
        Long productId,
        @Schema(description = "사전 재료 카테고리 ID", example = "1")
        Long productCategoryId,
        @Schema(description = "재료명", example = "두부")
        String productName,
        @Schema(description = "수량", example = "1모")
        String quantity,
        @Schema(description = "유통기한", example = "2026-08-04")
        LocalDate expiryDate,
        @Schema(description = "메모", example = "찌개용")
        String memo
){

}
