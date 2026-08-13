package com.naengpa.naengpamasterbackend.agent.shopping.client.dto;

import com.naengpa.naengpamasterbackend.product.entity.Product;

public record AgentProductPayload(
        Long productId,
        Long productCategoryId,
        String productName
) {
    public static AgentProductPayload from(Product product) {
        return new AgentProductPayload(
                product.getProductId(),
                product.getProductCategoryId(),
                product.getName()
        );
    }
}
