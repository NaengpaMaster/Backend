package com.naengpa.naengpamasterbackend.agent.shopping.dto.response;

import java.util.List;

public record ShoppingRecommendationItemResponse (
        Long productId,
        Long productCategoryId,
        String productName,
        String quantity,
        String reason
) {}