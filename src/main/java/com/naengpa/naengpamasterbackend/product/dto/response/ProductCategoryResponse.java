package com.naengpa.naengpamasterbackend.product.dto.response;

import com.naengpa.naengpamasterbackend.product.entity.ProductCategory;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductCategoryResponse (
        @Schema(description = "카테고리 ID", example = "1")
        Long productCategoryId,
        @Schema(description = "카테고리명", example = "채소/과일")
        String name
){
    public static ProductCategoryResponse from(ProductCategory productCategory){
        return new ProductCategoryResponse(
                productCategory.getProductCategoryId(),
                productCategory.getName()
        );
    }
}
