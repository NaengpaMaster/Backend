package com.naengpa.naengpamasterbackend.agent.shopping.client.dto;

public record AgentShoppingRecommendationItemResponse(
        Long productId,
        Long productCategoryId,
        String productName,
        String quantity,
        String reason
) {
}
