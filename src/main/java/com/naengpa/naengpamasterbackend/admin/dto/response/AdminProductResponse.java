package com.naengpa.naengpamasterbackend.admin.dto.response;

import com.naengpa.naengpamasterbackend.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdminProductResponse(
        @Schema(description = "사전 재료 ID", example = "1")
        Long productId,
        @Schema(description = "사전 재료 카테고리 ID", example = "1")
        Long productCategoryId,
        @Schema(description = "재료명", example = "두부")
        String name,
        @Schema(description = "기본 유통기한 일수", example = "7")
        Integer defaultExpiryDays,
        @Schema(description = "활성 여부", example = "true")
        Boolean isActive
) {

    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
                product.getProductId(),
                product.getProductCategoryId(),
                product.getName(),
                product.getDefaultExpiryDays(),
                product.getIsActive()
        );
    }
}
